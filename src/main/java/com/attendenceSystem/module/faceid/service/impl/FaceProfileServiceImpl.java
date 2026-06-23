package com.attendenceSystem.module.faceid.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.faceid.dto.request.CreateFaceProfileRequest;
import com.attendenceSystem.module.faceid.dto.request.UpdateFaceProfileRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceProfileResponse;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.mapper.request.CreateFaceProfileRequestMapper;
import com.attendenceSystem.module.faceid.mapper.response.FaceProfileResponseMapper;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.service.FaceProfileService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FaceProfileServiceImpl implements FaceProfileService {

    private final FaceProfileRepository faceProfileRepository;
    private final UserRepository userRepository;
    private final FaceProfileResponseMapper mapper;

    @Transactional
    @Override
    public FaceProfileResponse createFaceProfile(CreateFaceProfileRequest request) {
        validateCreateRequest(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (faceProfileRepository.findByFaceCode(request.getFaceCode()).isPresent()) {
            throw new IllegalArgumentException("Face code đã tồn tại");
        }

        FaceProfile faceProfile = CreateFaceProfileRequestMapper.toEntity(request, user);
        FaceProfile saved = faceProfileRepository.save(faceProfile);
        return mapper.fromEntity(saved);
    }

    @Transactional
    @Override
    public FaceProfileResponse updateFaceProfile(UpdateFaceProfileRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Face profile không hợp lệ");
        }

        FaceProfile saved = faceProfileRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Face profile không tồn tại"));

        if (request.getFaceCode() != null && !request.getFaceCode().equals(saved.getFaceCode())) {
            faceProfileRepository.findByFaceCode(request.getFaceCode()).ifPresent(existing -> {
                if (!existing.getId().equals(request.getId())) {
                    throw new IllegalArgumentException("Face code đã tồn tại");
                }
            });
            saved.setFaceCode(request.getFaceCode());
        }

        if (request.getSampleCount() != null) {
            saved.setSampleCount(request.getSampleCount());
        }
        if (request.getThumbnailUrl() != null) {
            saved.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getEmbeddingPath() != null) {
            saved.setEmbeddingPath(request.getEmbeddingPath());
        }

        FaceProfile updated = faceProfileRepository.save(saved);
        return mapper.fromEntity(updated);
    }

    @Override
    public FaceProfileResponse getFaceProfile(Long id) {
        FaceProfile faceProfile = faceProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Face profile không tìm thấy"));
        return mapper.fromEntity(faceProfile);
    }

    @Override
    public FaceProfileResponse getByFaceCode(String faceCode) {
        return faceProfileRepository.findByFaceCode(faceCode)
                .map(mapper::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Face profile không tìm thấy"));
    }

    @Override
    public Page<FaceProfileResponse> searchFaceProfiles(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return faceProfileRepository.findAll(pageable).map(mapper::fromEntity);
        }
        return faceProfileRepository.findByFaceCodeContainingIgnoreCase(query, pageable)
                .map(mapper::fromEntity);
    }

    private void validateCreateRequest(CreateFaceProfileRequest request) {
        if (request == null
                || request.getUserId() == null
                || request.getFaceCode() == null
                || request.getFaceCode().isBlank()) {
            throw new IllegalArgumentException("Dữ liệu face profile không hợp lệ");
        }
    }
}
