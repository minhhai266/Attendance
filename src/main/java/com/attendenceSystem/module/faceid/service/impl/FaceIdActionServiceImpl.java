package com.attendenceSystem.module.faceid.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.exception.custom.ResourceNotFoundException;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.service.FaceIdActionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FaceIdActionServiceImpl implements FaceIdActionService {
    private final FaceProfileRepository faceProfileRepository;

    @Transactional
    @Override
    public void acceptFaceId(Long id) {
        FaceProfile faceProfile = getFaceProfileById(id);
        if (faceProfile.getIsAccept() != null) {
            throw new IllegalStateException("Dữ liệu Face ID này đã được xử lý");
        }
        faceProfile.setIsAccept(true);
        faceProfileRepository.save(faceProfile);
    }

    @Transactional
    @Override
    public void rejectFaceId(Long id) {
        FaceProfile faceProfile = getFaceProfileById(id);
        if (faceProfile.getIsAccept() != null) {
            throw new IllegalStateException("Dữ liệu Face ID này đã được xử lý");
        }
        faceProfile.setIsAccept(false);

        faceProfileRepository.save(faceProfile);
    }

    private FaceProfile getFaceProfileById(Long id) {
        return faceProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu Face ID này"));
    }

}
