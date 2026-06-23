package com.attendenceSystem.module.faceid.mapper.request;

import com.attendenceSystem.module.faceid.dto.request.CreateFaceProfileRequest;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.user.entity.User;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CreateFaceProfileRequestMapper {
    public FaceProfile toEntity(CreateFaceProfileRequest request, User user) {

        return FaceProfile.builder()
                .user(user)
                .faceCode(request.getFaceCode())
                .sampleCount(request.getSampleCount())
                .thumbnailUrl(request.getThumbnailUrl())
                .embeddingPath(request.getEmbeddingPath())
                .build();
    }
}
