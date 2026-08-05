from typing import List, Optional

from fastapi import APIRouter, Depends, Header, HTTPException
from pydantic import BaseModel

from app.core.config import settings
from app.services.face_service import face_service

router = APIRouter(prefix="/api/internal/face", tags=["internal-face"])


def verify_internal_key(x_api_key: Optional[str] = Header(None)):
    expected = getattr(settings, "internal_api_key", None)
    if not expected or x_api_key != expected:
        raise HTTPException(status_code=401, detail="Invalid internal API key")


class SingleImageRequest(BaseModel):
    image_base64: str  # dạng "data:image/jpeg;base64,..." hoặc base64 thuần


class BatchImageRequest(BaseModel):
    images_base64: List[str]  # danh sách nhiều ảnh (VD: 5 góc mặt)


@router.post("/embed", dependencies=[Depends(verify_internal_key)])
def embed_single(payload: SingleImageRequest):
    try:
        image = face_service.read_image_from_base64(payload.image_base64)
        embedding, bbox = face_service.extract_single_embedding(image)
        return {
            "success": True,
            "embedding": face_service.serialize_embedding(embedding),
            "bbox": bbox,
        }
    except ValueError as e:
        return {"success": False, "message": str(e)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý ảnh: {e}")


@router.post("/embed-batch", dependencies=[Depends(verify_internal_key)])
def embed_batch(payload: BatchImageRequest):
    results = []
    for idx, image_b64 in enumerate(payload.images_base64):
        try:
            image = face_service.read_image_from_base64(image_b64)
            embedding, bbox = face_service.extract_single_embedding(image)
            results.append({
                "index": idx,
                "success": True,
                "embedding": face_service.serialize_embedding(embedding),
                "bbox": bbox,
            })
        except ValueError as e:
            results.append({"index": idx, "success": False, "message": str(e)})
        except Exception as e:
            results.append({"index": idx, "success": False, "message": f"Lỗi xử lý: {e}"})
    return {"results": results}

class PoseCheckRequest(BaseModel):
    image_base64: str


@router.post("/pose-check", dependencies=[Depends(verify_internal_key)])
def pose_check(payload: PoseCheckRequest):
    try:
        image = face_service.read_image_from_base64(payload.image_base64)
        result = face_service.analyze_single_face_pose(image)
        return result
    except Exception as e:
        return {"ok": False, "message": f"Lỗi xử lý: {e}", "face_count": 0}
class KnownFace(BaseModel):
    student_id: int
    student_code: str
    full_name: str
    embedding_base64: str


class RecognizeRequest(BaseModel):
    image_base64: str
    known_faces: List[KnownFace]
    threshold: Optional[float] = None


@router.post("/recognize", dependencies=[Depends(verify_internal_key)])
def recognize(payload: RecognizeRequest):
    try:
        image = face_service.read_image_from_base64(payload.image_base64)
        known = [
            {
                "student_id": f.student_id,
                "student_code": f.student_code,
                "full_name": f.full_name,
                "embedding": face_service.deserialize_embedding(f.embedding_base64),
            }
            for f in payload.known_faces
        ]
        threshold = payload.threshold if payload.threshold is not None else settings.face_threshold
        results = face_service.recognize_faces(image, known, threshold)
        return {"success": True, "results": results}
    except ValueError as e:
        return {"success": False, "message": str(e)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi nhận diện: {e}")