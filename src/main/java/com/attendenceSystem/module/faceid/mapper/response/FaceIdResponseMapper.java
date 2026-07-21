package com.attendenceSystem.module.faceid.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.faceid.dto.response.FaceIdResponse;
import com.attendenceSystem.module.faceid.dto.response.FaceProfileResponse;

@Component
public class FaceIdResponseMapper {

    public FaceIdResponse fromEntity(FaceProfileResponse faceProfile) {
        if (faceProfile == null) {
            return null;
        }
        return FaceIdResponse.builder()
                .id(faceProfile.id())
                .faceCode(faceProfile.faceCode())
                .sampleCount(faceProfile.sampleCount())
                .thumbnailUrl(faceProfile.thumbnailUrl())
                .userFullName(faceProfile.userFullName())
                .userEmail(faceProfile.userEmail())
                .build();
    }
}