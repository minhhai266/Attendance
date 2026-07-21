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
public class FaceCaptureResponse {
    private String faceCode;
    private String thumbnailUrl;
    private String message;
    private boolean success;
}