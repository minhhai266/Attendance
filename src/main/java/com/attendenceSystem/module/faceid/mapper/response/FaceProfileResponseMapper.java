package com.attendenceSystem.module.faceid.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.faceid.dto.response.FaceProfileResponse;
import com.attendenceSystem.module.faceid.entity.FaceProfile;

@Component
public class FaceProfileResponseMapper {

    public FaceProfileResponse fromEntity(FaceProfile faceProfile) {
        if (faceProfile == null) {
            return null;
        }
        return FaceProfileResponse.builder()
                .id(faceProfile.getId())
                .userId(faceProfile.getUser() != null ? faceProfile.getUser().getId() : null)
                .userName(faceProfile.getUser() != null ? faceProfile.getUser().getUsername() : null)
                .userEmail(faceProfile.getUser() != null ? faceProfile.getUser().getEmail() : null)
                .userFullName(faceProfile.getUser() != null ? faceProfile.getUser().getFullName() : null)
                .faceCode(faceProfile.getFaceCode())
                .sampleCount(faceProfile.getSampleCount())
                .thumbnailUrl(faceProfile.getThumbnailUrl())
                .embeddingPath(faceProfile.getEmbeddingPath())
                .createdAt(faceProfile.getCreatedAt())
                .updatedAt(faceProfile.getUpdatedAt())
                .build();
    }
}
