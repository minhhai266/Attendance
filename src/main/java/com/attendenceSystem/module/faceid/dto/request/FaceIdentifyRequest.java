package com.attendenceSystem.module.faceid.dto.request;

import lombok.Data;

@Data
public class FaceIdentifyRequest {
    private String imageBase64;
}