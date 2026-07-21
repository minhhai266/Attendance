package com.attendenceSystem.module.faceid.dto.response;

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
public class LatestFaceResponse {
    private String faceCode;
    private String imagePath;  // đường dẫn ảnh để frontend load
    private Long sampleId;
}