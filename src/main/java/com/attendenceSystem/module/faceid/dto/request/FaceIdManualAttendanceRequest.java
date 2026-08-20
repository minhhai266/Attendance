package com.attendenceSystem.module.faceid.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request điểm danh thủ công khi hệ thống AI (face-ai) không thể kết nối.
 * Không có "confidence" vì không qua bước nhận diện AI - người dùng tự chọn
 * nhân viên từ danh sách trên giao diện offline.
 */
@Data
public class FaceIdManualAttendanceRequest {

    @NotBlank(message = "employeeCode không được để trống")
    private String employeeCode;

    /** Ảnh chụp để lưu vết (audit), không dùng để nhận diện. */
    private String imageBase64;

    @NotNull(message = "capturedAt không được để trống")
    private LocalDateTime capturedAt;

    private String cameraId;

    @Size(max = 255, message = "trackingId không được vượt quá 255 ký tự")
    private String trackingId;
}