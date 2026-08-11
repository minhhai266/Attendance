package com.attendenceSystem.module.faceid.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.faceid.dto.request.FaceCaptureRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceCaptureResponse;
import com.attendenceSystem.module.faceid.dto.response.LatestFaceResponse;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.entity.FaceSample;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.repository.FaceSampleRepository;
import com.attendenceSystem.module.faceid.service.FaceAiClient;
import com.attendenceSystem.module.faceid.service.FaceEmbeddingCacheService;
import com.attendenceSystem.module.faceid.service.FaceIdCaptureService;
import com.attendenceSystem.module.storage.exception.FileStorageException;
import com.attendenceSystem.module.storage.provider.StorageProvider;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaceIdCaptureServiceImpl implements FaceIdCaptureService {

    private static final String FACE_SAMPLE_DIRECTORY = "face_samples";

    private final FaceAiClient faceAiClient;
    private final FaceProfileRepository faceProfileRepository;
    private final FaceSampleRepository faceSampleRepository;
    private final UserRepository userRepository;
    private final StorageProvider storageProvider;
    private final FaceEmbeddingCacheService faceEmbeddingCacheService;

    @Override
    @Transactional
    public FaceCaptureResponse registerFace(FaceCaptureRequest request) {
        User user = getCurrentUser();

        FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                .orElseGet(() -> faceProfileRepository.save(
                        FaceProfile.builder()
                                .user(user)
                                .faceCode(UUID.randomUUID().toString())
                                .sampleCount(0)
                                .build()));

        cleanUpOldSamples(faceProfile);
        faceSampleRepository.deleteByFaceProfile(faceProfile);

        int sampleCount = 0;
        String lastImagePath = null;

        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            FaceAiClient.EmbedBatchResponse aiResult = faceAiClient.embedBatch(request.getSamples());

            for (int i = 0; i < request.getSamples().size(); i++) {
                FaceAiClient.EmbedResult embedResult = aiResult.results.get(i);

                if (!embedResult.success) {
                    log.warn("Mẫu {} lỗi: {}", i + 1, embedResult.message);
                    continue;
                }

                String imagePath = saveBase64Image(request.getSamples().get(i), user.getId(), i + 1);

                faceSampleRepository.save(FaceSample.builder()
                        .faceProfile(faceProfile)
                        .imagePath(imagePath)
                        .sampleOrder(i + 1)
                        .embedding(embedResult.embedding)
                        .build());

                sampleCount++;
                lastImagePath = imagePath;
            }
        }

        if (sampleCount == 0) {
            return FaceCaptureResponse.builder()
                    .message("Không nhận diện được khuôn mặt hợp lệ nào trong các mẫu đã chụp")
                    .success(false)
                    .build();
        }

        faceProfile.setSampleCount(sampleCount);
        faceProfile.setThumbnailUrl(lastImagePath);
        faceProfile.setIsAccept(null);
        faceProfileRepository.save(faceProfile);
        faceEmbeddingCacheService.invalidate();

        return FaceCaptureResponse.builder()
                .faceCode(faceProfile.getFaceCode())
                .thumbnailUrl(lastImagePath)
                .message("Đăng ký thành công " + sampleCount + " mẫu khuôn mặt")
                .success(true)
                .build();
    }

    @Override
    public FaceAiClient.PoseResult checkPose(String imageBase64) {
        try {
            return faceAiClient.checkPose(imageBase64);
        } catch (Exception e) {
            log.error("Lỗi kiểm tra tư thế", e);
            FaceAiClient.PoseResult error = new FaceAiClient.PoseResult();
            error.ok = false;
            error.message = "Không kết nối được AI";
            return error;
        }
    }

    @Override
    public LatestFaceResponse getLatestFace() {
        User currentUser = getCurrentUser();
        FaceProfile faceProfile = faceProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Chưa đăng ký face"));

        return LatestFaceResponse.builder()
                .faceCode(faceProfile.getFaceCode())
                .imagePath(faceProfile.getThumbnailUrl())
                .username(currentUser.getUsername())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .isAccept(faceProfile.getIsAccept())
                .build();
    }

    @Override
    @Transactional
    public FaceCaptureResponse updateForAttendance(FaceCaptureRequest request) {
        User user = getCurrentUser();
        FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Chưa đăng ký face"));

        String oldImagePath = faceProfile.getThumbnailUrl();
        if (oldImagePath != null) {
            deleteFileSafely(oldImagePath);
        }

        String newImagePath = null;
        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            newImagePath = saveBase64Image(request.getSamples().get(0), user.getId(), 1);
        }

        faceProfile.setThumbnailUrl(newImagePath);
        faceProfile.setIsAccept(null);
        faceProfileRepository.save(faceProfile);
        faceEmbeddingCacheService.invalidate();

        return FaceCaptureResponse.builder()
                .faceCode(faceProfile.getFaceCode())
                .thumbnailUrl(newImagePath)
                .message("Cập nhật ảnh điểm danh thành công")
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public void deleteFaceSample(Long sampleId) {
        User user = getCurrentUser();
        FaceSample sample = faceSampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu"));

        if (!sample.getFaceProfile().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Không có quyền xóa mẫu này");
        }

        faceSampleRepository.deleteById(sampleId);
        deleteFileSafely(sample.getImagePath());
        faceEmbeddingCacheService.invalidate();
    }

    private User getCurrentUser() {
        return userRepository.findByUsername(SecurityUtil.getCurrentUserName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }

    private void cleanUpOldSamples(FaceProfile faceProfile) {
        List<FaceSample> oldSamples = faceSampleRepository.findByFaceProfile(faceProfile);
        for (FaceSample oldSample : oldSamples) {
            deleteFileSafely(oldSample.getImagePath());
        }
    }

    private void deleteFileSafely(String url) {
        try {
            String path = extractPathFromUrl(url);
            if (path != null) {
                storageProvider.delete(path);
            }
        } catch (FileStorageException e) {
            log.warn("Không thể xóa file: {}", url);
        }
    }

    private String saveBase64Image(String base64Image, Long userId, int order) {
        try {
            String imageContent = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
            byte[] imageBytes = Base64.getDecoder().decode(imageContent);

            String fileName = "face_" + userId + "_" + order + ".jpg";
            Path targetPath = Paths.get(FACE_SAMPLE_DIRECTORY, String.valueOf(userId), fileName);

            storageProvider.save(targetPath, imageBytes);

            return "/uploads/" + FACE_SAMPLE_DIRECTORY + "/" + userId + "/" + fileName;
        } catch (Exception e) {
            log.error("Lỗi khi lưu ảnh base64", e);
            throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage());
        }
    }

    private String extractPathFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url.startsWith("/uploads/") ? url.substring(9) : url;
    }
}
