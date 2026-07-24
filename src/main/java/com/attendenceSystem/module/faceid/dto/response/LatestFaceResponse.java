package com.attendenceSystem.module.faceid.dto.response;

import lombok.Builder;

@Builder
public record LatestFaceResponse(
        String faceCode,
        String imagePath,
        Long sampleId) {
}