package com.attendenceSystem.module.faceid.dto.response;

import lombok.Builder;

@Builder
public record FaceIdResponse(
        Long id,
        String faceCode,
        Integer sampleCount,
        String thumbnailUrl,
        String userFullName,
        String userEmail) {
}