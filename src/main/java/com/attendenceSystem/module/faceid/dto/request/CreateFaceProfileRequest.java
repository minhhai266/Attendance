package com.attendenceSystem.module.faceid.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFaceProfileRequest {
    private Long userId;
    private String faceCode;
    private Integer sampleCount;
    private String thumbnailUrl;
    private String embeddingPath;
}
