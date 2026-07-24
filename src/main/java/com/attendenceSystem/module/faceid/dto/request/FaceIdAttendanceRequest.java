package com.attendenceSystem.module.faceid.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FaceIdAttendanceRequest {
    @NotBlank(message = "studentCode không được để trống")
    private String studentCode;

    @NotNull(message = "confidence không được để trống")
    private Double confidence;

    @NotNull(message = "capturedAt không được để trống")
    private LocalDateTime capturedAt;

    private String cameraId;

    private String imageId;

    @Size(max = 255, message = "trackingId không được vượt quá 255 ký tự")
    private String trackingId;

    private Boolean liveness;
}