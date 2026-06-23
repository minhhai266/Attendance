package com.attendenceSystem.module.faceid.dto.response;

import java.time.Instant;

import lombok.Builder;

@Builder
public record FaceProfileResponse(
        Long id,
        Long userId,
        String faceCode,
        Integer sampleCount,
        String thumbnailUrl,
        String embeddingPath,
        String userName,
        String userEmail,
        String userFullName,
        Instant createdAt,
        Instant updatedAt) {
}
