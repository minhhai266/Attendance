import threading
from collections.abc import Callable


class FaceEmbeddingCache:
    def __init__(self):
        self._lock = threading.Lock()
        self._known_faces = None

    def get_known_faces(self, loader: Callable[[], list[dict]]) -> list[dict]:
        with self._lock:
            if self._known_faces is None:
                self._known_faces = loader()
            return self._known_faces

    def invalidate(self) -> None:
        with self._lock:
            self._known_faces = None


face_embedding_cache = FaceEmbeddingCache()
