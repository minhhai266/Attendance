from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    app_name: str = "Face Lab System"
    secret_key: str = "change-this-secret"
    database_path: str = "data/face_lab.db"
    insightface_model: str = "buffalo_l"
    insightface_det_size: int = 640
    face_threshold: float = 0.55
    check_cooldown_seconds: int = 30
    frame_skip: int = 1
    check_in_camera_device_id: str = ""
    check_out_camera_device_id: str = ""
    default_admin_username: str = "admin"
    default_admin_password: str = "admin123"
    login_max_failed_attempts: int = 10
    login_attempt_window_seconds: int = 15 * 60
    login_lockout_seconds: int = 15 * 60
    session_max_age_seconds: int = 8 * 60 * 60
    https_enabled: bool = False
    trusted_hosts: str = "127.0.0.1,localhost,testserver"
    public_docs_enabled: bool = False
    health_details_enabled: bool = False
    password_pbkdf2_iterations: int = 600_000
    websocket_allowed_origins: str = "http://127.0.0.1:8002,http://localhost:8002"
    websocket_max_message_bytes: int = 512 * 1024
    websocket_max_image_bytes: int = 384 * 1024
    websocket_max_image_pixels: int = 1_000_000

    # Anti-spoofing / liveness detection
    # Default: best_model_quantized.onnx from the lightweight face-antispoof ONNX repo.
    liveness_enabled: bool = True
    anti_spoof_model_path: str = "models/anti_spoofing/best_model_quantized.onnx"
    liveness_threshold: float = 0.55
    liveness_real_class_index: int = 0
    liveness_input_size: int = 128
    liveness_crop_scale: float = 1.5
    liveness_min_face_size: int = 80
    liveness_min_brightness: float = 35.0
    liveness_min_blur: float = 18.0
    liveness_edge_margin: int = 5
    missing_checkout_cutoff_time: str = "23:59"
    missing_checkout_scan_interval_seconds: int = 60
    work_start_time: str = "08:00"
    work_end_time: str = "17:00"
    late_grace_minutes: int = 5
    early_leave_grace_minutes: int = 10


settings = Settings()
