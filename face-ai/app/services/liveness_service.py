from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional, Sequence, Tuple

import cv2
import numpy as np

from app.core.config import settings


@dataclass
class LivenessResult:
    enabled: bool
    model_loaded: bool
    is_live: bool
    real_score: Optional[float]
    fake_score: Optional[float]
    real_class_index: Optional[int]
    raw_scores: Optional[List[float]]
    label: str
    message: str

    def to_dict(self) -> Dict[str, Any]:
        return {
            "enabled": self.enabled,
            "model_loaded": self.model_loaded,
            "is_live": self.is_live,
            "real_score": self.real_score,
            "fake_score": self.fake_score,
            "real_class_index": self.real_class_index,
            "raw_scores": self.raw_scores,
            "label": self.label,
            "message": self.message,
        }


class LivenessService:
    """Run ONNX face anti-spoofing for the realtime camera flow.

    The default model is best_model_quantized.onnx from the lightweight
    face-antispoof ONNX repo. That model expects an RGB 128x128 crop in
    0..1 range and returns two logits: [real, spoof].
    """

    def __init__(self):
        self.session = None
        self.loaded = False
        self.load_error: Optional[str] = None
        self.input_name: Optional[str] = None
        self.input_hw: Tuple[int, int] = (settings.liveness_input_size, settings.liveness_input_size)

    @staticmethod
    def _setting(key: str, default: Any) -> Any:
        try:
            from app.db import get_setting

            return get_setting(key, default)
        except Exception:
            return default

    def is_enabled(self) -> bool:
        default = "true" if settings.liveness_enabled else "false"
        value = self._setting("liveness_enabled", default)
        return str(value).lower() in {"1", "true", "yes", "on"}

    def threshold(self) -> float:
        return float(self._setting("liveness_threshold", settings.liveness_threshold))

    def real_class_index(self) -> int:
        return int(self._setting("liveness_real_class_index", settings.liveness_real_class_index))

    def crop_scale(self) -> float:
        return float(self._setting("liveness_crop_scale", settings.liveness_crop_scale))

    def load(self) -> None:
        if not self.is_enabled():
            return
        if self.loaded:
            return
        model_path = Path(settings.anti_spoof_model_path)
        if not model_path.exists():
            self.load_error = f"Không tìm thấy model anti-spoofing: {model_path}"
            self.loaded = False
            return
        try:
            import onnxruntime as ort

            self.session = ort.InferenceSession(str(model_path), providers=["CPUExecutionProvider"])
            model_input = self.session.get_inputs()[0]
            self.input_name = model_input.name
            shape = list(model_input.shape)
            # Thường là [1, 3, H, W]. Nếu model có shape cố định thì tự lấy H/W.
            if len(shape) == 4 and isinstance(shape[2], int) and isinstance(shape[3], int):
                self.input_hw = (int(shape[2]), int(shape[3]))
            self.loaded = True
            self.load_error = None
        except Exception as exc:
            self.load_error = str(exc)
            self.loaded = False

    def ensure_loaded(self) -> bool:
        if not self.is_enabled():
            return False
        if not self.loaded:
            self.load()
        return self.loaded

    @staticmethod
    def _expanded_square_crop(frame_rgb: np.ndarray, bbox: Sequence[float], expansion_factor: float = 1.5) -> np.ndarray:
        height, width = frame_rgb.shape[:2]
        x1, y1, x2, y2 = [float(v) for v in bbox[:4]]
        bw = x2 - x1
        bh = y2 - y1
        if bw <= 0 or bh <= 0:
            raise ValueError("Invalid face bbox for liveness crop.")

        max_dim = max(bw, bh)
        center_x = x1 + bw / 2
        center_y = y1 + bh / 2
        crop_size = max(2, int(max_dim * expansion_factor))
        left = int(center_x - crop_size / 2)
        top = int(center_y - crop_size / 2)
        right = left + crop_size
        bottom = top + crop_size

        crop_x1 = max(0, left)
        crop_y1 = max(0, top)
        crop_x2 = min(width, right)
        crop_y2 = min(height, bottom)
        if crop_x2 <= crop_x1 or crop_y2 <= crop_y1:
            raise ValueError("Cannot crop face for liveness.")

        crop = frame_rgb[crop_y1:crop_y2, crop_x1:crop_x2, :]
        top_pad = max(0, -top)
        bottom_pad = max(0, bottom - height)
        left_pad = max(0, -left)
        right_pad = max(0, right - width)
        if top_pad or bottom_pad or left_pad or right_pad:
            crop = cv2.copyMakeBorder(
                crop,
                top_pad,
                bottom_pad,
                left_pad,
                right_pad,
                cv2.BORDER_REFLECT_101,
            )
        return crop

    @staticmethod
    def _letterbox(face_crop: np.ndarray, input_h: int, input_w: int) -> np.ndarray:
        old_h, old_w = face_crop.shape[:2]
        ratio = min(float(input_h) / old_h, float(input_w) / old_w)
        scaled_h = max(1, int(old_h * ratio))
        scaled_w = max(1, int(old_w * ratio))
        interpolation = cv2.INTER_LANCZOS4 if ratio > 1.0 else cv2.INTER_AREA
        face_crop = cv2.resize(face_crop, (scaled_w, scaled_h), interpolation=interpolation)

        delta_h = input_h - scaled_h
        delta_w = input_w - scaled_w
        top = delta_h // 2
        bottom = delta_h - top
        left = delta_w // 2
        right = delta_w - left
        return cv2.copyMakeBorder(face_crop, top, bottom, left, right, cv2.BORDER_REFLECT_101)

    def _preprocess(self, frame: np.ndarray, bbox: Sequence[float]) -> np.ndarray:
        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        face_crop = self._expanded_square_crop(frame_rgb, bbox, self.crop_scale())
        if face_crop.size == 0:
            raise ValueError("Cannot crop face for liveness.")

        input_h, input_w = self.input_hw
        face_crop = self._letterbox(face_crop, input_h, input_w)
        face_crop = face_crop.astype(np.float32) / 255.0
        face_crop = np.transpose(face_crop, (2, 0, 1))
        face_crop = np.expand_dims(face_crop, axis=0)
        return face_crop

    @staticmethod
    def _softmax(x: np.ndarray) -> np.ndarray:
        x = x.astype(np.float32).reshape(-1)
        x = x - np.max(x)
        exp = np.exp(x)
        total = np.sum(exp)
        if total == 0:
            return exp
        return exp / total

    @staticmethod
    def _sigmoid(value: float) -> float:
        if value >= 0:
            z = np.exp(-value)
            return float(1.0 / (1.0 + z))
        z = np.exp(value)
        return float(z / (1.0 + z))

    def _scores_from_output(self, raw: np.ndarray) -> Tuple[float, float, int, List[float]]:
        raw = raw.astype(np.float32).reshape(-1)
        if len(raw) == 2:
            real_logit = float(raw[0])
            spoof_logit = float(raw[1])
            real_score = self._sigmoid(real_logit - spoof_logit)
            fake_score = 1.0 - real_score
            return real_score, fake_score, 0, [real_score, fake_score]

        probs = self._softmax(raw)
        real_idx = self.real_class_index()
        if real_idx < 0 or real_idx >= len(probs):
            real_idx = int(np.argmax(probs))
        real_score = float(probs[real_idx])
        fake_score = float(1.0 - real_score)
        return real_score, fake_score, real_idx, [float(p) for p in probs]

    def predict(self, frame: np.ndarray, bbox: Sequence[float]) -> LivenessResult:
        if not self.is_enabled():
            return LivenessResult(False, False, True, None, None, None, None, "disabled", "Liveness đang tắt.")

        if not self.ensure_loaded():
            # Fail-safe: bật liveness nhưng model lỗi thì coi là không đạt để tránh cho qua giả mạo.
            return LivenessResult(True, False, False, None, None, None, None, "model_error", self.load_error or "Không load được model anti-spoofing.")

        try:
            inp = self._preprocess(frame, bbox)
            outputs: List[np.ndarray] = self.session.run(None, {self.input_name: inp})  # type: ignore[union-attr]
            raw = np.asarray(outputs[0]).reshape(-1)
            real_score, fake_score, real_idx, scores = self._scores_from_output(raw)
            is_live = real_score >= self.threshold()
            return LivenessResult(
                True,
                True,
                is_live,
                round(real_score, 4),
                round(fake_score, 4),
                real_idx,
                [round(float(p), 4) for p in scores],
                "real" if is_live else "fake",
                "Mặt thật" if is_live else "Nghi ngờ giả mạo khuôn mặt",
            )
        except Exception as exc:
            return LivenessResult(True, True, False, None, None, None, None, "error", f"Lỗi liveness: {exc}")


liveness_service = LivenessService()
