package com.attendenceSystem.module.faceid.dto.response;

import lombok.Builder;

@Builder
public record FaceCaptureResponse(
        String faceCode,
        String thumbnailUrl,
        String message,
        boolean success) {
}