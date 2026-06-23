package com.attendenceSystem.module.faceid.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.faceid.dto.request.CreateFaceProfileRequest;
import com.attendenceSystem.module.faceid.dto.request.UpdateFaceProfileRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceProfileResponse;

public interface FaceProfileService {
    FaceProfileResponse createFaceProfile(CreateFaceProfileRequest request);
    FaceProfileResponse updateFaceProfile(UpdateFaceProfileRequest request);
    FaceProfileResponse getFaceProfile(Long id);
    FaceProfileResponse getByFaceCode(String faceCode);
    Page<FaceProfileResponse> searchFaceProfiles(String query, Pageable pageable);
}
