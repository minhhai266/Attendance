import base64
import os
from datetime import datetime
from typing import Any, Dict, List, Optional, Tuple

import cv2
import numpy as np
from app.core.config import settings


class FaceService:
    def __init__(self):
        self.app = None
        self.loaded = False
        self.load_error = None

    def load(self):
        if self.loaded:
            return
        try:
            from insightface.app import FaceAnalysis
            self.app = FaceAnalysis(
                name=settings.insightface_model,
                allowed_modules=["detection", "recognition"],
                providers=["CPUExecutionProvider"],
            )
            det_size = int(settings.insightface_det_size)
            self.app.prepare(ctx_id=-1, det_size=(det_size, det_size))
            self.loaded = True
        except Exception as exc:
            self.load_error = str(exc)
            self.loaded = False

    def ensure_loaded(self):
        if not self.loaded:
            self.load()
        if not self.loaded:
            raise RuntimeError(f"InsightFace chưa load được model: {self.load_error}")

    @staticmethod
    def read_image_from_bytes(data: bytes):
        arr = np.frombuffer(data, np.uint8)
        img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
        if img is None:
            raise ValueError("Không đọc được ảnh.")
        return img

    @staticmethod
    def read_image_from_base64(data_url: str):
        if "," in data_url:
            data_url = data_url.split(",", 1)[1]
        data = base64.b64decode(data_url)
        return FaceService.read_image_from_bytes(data)

    def get_faces(self, image) -> list:
        self.ensure_loaded()
        return self.app.get(image)

    def extract_single_embedding(self, image) -> Tuple[np.ndarray, list]:
        faces = self.get_faces(image)
        if len(faces) == 0:
            raise ValueError("Không tìm thấy khuôn mặt trong ảnh.")
        if len(faces) > 1:
            raise ValueError("Ảnh có nhiều hơn 1 khuôn mặt, vui lòng chọn ảnh chỉ có 1 người.")
        face = faces[0]
        embedding = np.asarray(face.normed_embedding, dtype=np.float32)
        bbox = face.bbox.astype(int).tolist()
        return embedding, bbox

    def analyze_single_face_pose(self, image) -> Dict[str, Any]:
        faces = self.get_faces(image)
        height, width = image.shape[:2]
        if len(faces) == 0:
            return {"ok": False, "message": "Không tìm thấy khuôn mặt.", "face_count": 0}
        if len(faces) > 1:
            return {"ok": False, "message": "Chỉ nên có 1 khuôn mặt trong khung.", "face_count": len(faces)}

        face = faces[0]
        bbox = face.bbox.astype(float)
        x1, y1, x2, y2 = bbox.tolist()
        face_w = max(x2 - x1, 1.0)
        face_h = max(y2 - y1, 1.0)
        face_ratio = min(face_w / max(width, 1), face_h / max(height, 1))
        bbox_int = [int(x1), int(y1), int(x2), int(y2)]
        if face_ratio < 0.16:
            return {
                "ok": False,
                "message": "Đưa mặt lại gần camera hơn.",
                "face_count": 1,
                "bbox": bbox_int,
                "face_ratio": round(float(face_ratio), 3),
            }

        kps = np.asarray(getattr(face, "kps", []), dtype=np.float32)
        if kps.shape[0] < 5:
            return {"ok": False, "message": "Không đọc được landmark khuôn mặt.", "face_count": 1, "bbox": bbox_int}

        left_eye, right_eye, nose, left_mouth, right_mouth = kps[:5]
        eye_mid = (left_eye + right_eye) / 2.0
        mouth_mid = (left_mouth + right_mouth) / 2.0
        eye_dist = max(float(np.linalg.norm(right_eye - left_eye)), 1.0)
        face_vertical = max(float(np.linalg.norm(mouth_mid - eye_mid)), 1.0)

        yaw = float((nose[0] - eye_mid[0]) / eye_dist)
        pitch = float((nose[1] - eye_mid[1]) / face_vertical)

        if yaw < -0.16:
            pose = "left"
            message = "Giữ mặt quay trái ổn định."
        elif yaw > 0.16:
            pose = "right"
            message = "Giữ mặt quay phải ổn định."
        elif pitch < 0.38:
            pose = "up"
            message = "Giữ tư thế ngẩng cằm lên."
        elif pitch > 0.58:
            pose = "down"
            message = "Giữ tư thế cúi cằm xuống."
        else:
            pose = "front"
            message = "Giữ mặt nhìn thẳng."

        return {
            "ok": True,
            "message": message,
            "face_count": 1,
            "pose": pose,
            "yaw": round(yaw, 3),
            "pitch": round(pitch, 3),
            "bbox": bbox_int,
            "face_ratio": round(float(face_ratio), 3),
        }

    @staticmethod
    def serialize_embedding(embedding: np.ndarray) -> str:
        return base64.b64encode(embedding.astype(np.float32).tobytes()).decode("utf-8")

    @staticmethod
    def deserialize_embedding(data: str) -> np.ndarray:
        return np.frombuffer(base64.b64decode(data.encode("utf-8")), dtype=np.float32)

    @staticmethod
    def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
        denom = np.linalg.norm(a) * np.linalg.norm(b)
        if denom == 0:
            return 0.0
        return float(np.dot(a, b) / denom)

    @staticmethod
    def _setting_number(key: str, default, cast=float):
        try:
            from app.db import get_setting

            return cast(get_setting(key, default))
        except Exception:
            return default

    @staticmethod
    def evaluate_liveness_quality(image, bbox: List[int]) -> Dict[str, Any]:
        height, width = image.shape[:2]
        x1, y1, x2, y2 = [int(v) for v in bbox[:4]]
        face_w = max(0, x2 - x1)
        face_h = max(0, y2 - y1)
        min_face_size = FaceService._setting_number("liveness_min_face_size", settings.liveness_min_face_size, int)
        edge_margin = FaceService._setting_number("liveness_edge_margin", settings.liveness_edge_margin, int)

        metrics = {
            "face_width": face_w,
            "face_height": face_h,
            "min_face_size": min_face_size,
        }
        if face_w < min_face_size or face_h < min_face_size:
            return {
                "ok": False,
                "reason": "face_too_small",
                "message": "Khuôn mặt quá nhỏ. Vui lòng đưa mặt lại gần camera.",
                "metrics": metrics,
            }

        if x1 < edge_margin or y1 < edge_margin or width - x2 < edge_margin or height - y2 < edge_margin:
            return {
                "ok": False,
                "reason": "face_near_edge",
                "message": "Vui lòng đưa khuôn mặt vào giữa khung hình.",
                "metrics": metrics,
            }

        crop_x1 = max(0, x1)
        crop_y1 = max(0, y1)
        crop_x2 = min(width, x2)
        crop_y2 = min(height, y2)
        crop = image[crop_y1:crop_y2, crop_x1:crop_x2]
        if crop.size == 0:
            return {
                "ok": False,
                "reason": "empty_crop",
                "message": "Không cắt được khuôn mặt.",
                "metrics": metrics,
            }

        gray = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)
        brightness = float(np.mean(gray))
        blur = float(cv2.Laplacian(gray, cv2.CV_64F).var())
        min_brightness = FaceService._setting_number("liveness_min_brightness", settings.liveness_min_brightness, float)
        min_blur = FaceService._setting_number("liveness_min_blur", settings.liveness_min_blur, float)
        metrics.update({
            "brightness": round(brightness, 2),
            "min_brightness": min_brightness,
            "blur": round(blur, 2),
            "min_blur": min_blur,
        })

        if brightness < min_brightness:
            return {
                "ok": False,
                "reason": "too_dark",
                "message": "Ảnh quá tối. Vui lòng tăng ánh sáng.",
                "metrics": metrics,
            }
        if blur < min_blur:
            return {
                "ok": False,
                "reason": "too_blurry",
                "message": "Ảnh bị mờ. Vui lòng giữ mặt ổn định hơn.",
                "metrics": metrics,
            }
        return {"ok": True, "reason": "ok", "message": "OK", "metrics": metrics}

    def recognize_faces(self, image, known_faces: List[dict], threshold: float):
        faces = self.get_faces(image)
        results = []
        from app.services.liveness_service import LivenessResult, liveness_service

        for face in faces:
            bbox = face.bbox.astype(int).tolist()
            quality = self.evaluate_liveness_quality(image, bbox)
            liveness = None
            if liveness_service.is_enabled():
                if quality["ok"]:
                    liveness = liveness_service.predict(image, bbox)
                else:
                    liveness = LivenessResult(
                        True,
                        liveness_service.loaded,
                        False,
                        None,
                        None,
                        None,
                        None,
                        "quality_error",
                        quality["message"],
                    )
            else:
                liveness = liveness_service.predict(image, bbox)

            emb = np.asarray(face.normed_embedding, dtype=np.float32)
            best = None
            best_score = -1.0
            for known in known_faces:
                score = self.cosine_similarity(emb, known["embedding"])
                if score > best_score:
                    best_score = score
                    best = known

            liveness_dict = liveness.to_dict() if liveness is not None else None
            quality_status = str(quality.get("reason", "ok"))
            if not liveness or not liveness.enabled:
                liveness_status = "live"
            elif not quality.get("ok"):
                liveness_status = "uncertain"
            elif liveness.label == "real":
                liveness_status = "live"
            elif liveness.label == "fake":
                liveness_status = "fake"
            else:
                liveness_status = "uncertain"

            liveness_blocked = liveness_status in {"fake", "uncertain"}
            spoof_detected = liveness_status == "fake"
            note = None
            if liveness_status == "uncertain":
                note = quality.get("message") or "Ảnh chưa đủ chất lượng để kiểm tra. Vui lòng chỉnh lại camera."
            elif liveness_status == "fake" and liveness is not None:
                note = liveness.message

            if best and best_score >= threshold:
                results.append({
                    "bbox": bbox,
                    "student_id": best["student_id"],
                    "student_code": best["student_code"],
                    "full_name": best["full_name"],
                    "confidence": round(best_score, 4),
                    "recognized": True,
                    "spoof_detected": spoof_detected,
                    "liveness_blocked": liveness_blocked,
                    "liveness_status": liveness_status,
                    "quality_status": quality_status,
                    "liveness": liveness_dict,
                    "quality": quality,
                    "note": note,
                })
            else:
                results.append({
                    "bbox": bbox,
                    "student_id": None,
                    "student_code": "Unknown",
                    "full_name": "Unknown",
                    "confidence": round(best_score, 4) if best_score >= 0 else None,
                    "recognized": False,
                    "spoof_detected": spoof_detected,
                    "liveness_blocked": liveness_blocked,
                    "liveness_status": liveness_status,
                    "quality_status": quality_status,
                    "liveness": liveness_dict,
                    "quality": quality,
                    "note": note,
                })
        return results


face_service = FaceService()
