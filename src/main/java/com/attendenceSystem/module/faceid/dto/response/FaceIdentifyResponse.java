package com.attendenceSystem.module.faceid.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaceIdentifyResponse {
    private boolean faceDetected;
    private List<Integer> bbox;      // [x1, y1, x2, y2] toạ độ trên ảnh gốc
    private String pose;             // front/left/right/up/down
    private boolean matched;
    private String studentCode;
    private String fullName;
    private Double confidence;       // 0..1
    private String message;
}