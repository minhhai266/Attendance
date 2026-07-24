package com.attendenceSystem.module.faceid.api;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.faceid.dto.request.FaceCaptureRequest;
import com.attendenceSystem.module.faceid.dto.request.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceCaptureResponse;
import com.attendenceSystem.module.faceid.dto.response.FaceIdAttendanceResponse;
import com.attendenceSystem.module.faceid.dto.response.LatestFaceResponse;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.entity.FaceSample;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.repository.FaceSampleRepository;
import com.attendenceSystem.module.faceid.service.FaceIdAttendanceService;
import com.attendenceSystem.module.storage.provider.StorageProvider;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(Routes.API + Routes.FaceId.ROOT)
@RequiredArgsConstructor
@Slf4j
public class FaceIdApiController {

    private final FaceProfileRepository faceProfileRepository;
    private final FaceSampleRepository faceSampleRepository;
    private final UserRepository userRepository;
    private final StorageProvider storageProvider;
    private final FaceIdAttendanceService faceIdAttendanceService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<FaceCaptureResponse> registerFace(@RequestBody FaceCaptureRequest request) {
        try {
            User user = getCurrentUser();

            FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                    .orElseGet(() -> faceProfileRepository.save(
                            FaceProfile.builder()
                                    .user(user)
                                    .faceCode(UUID.randomUUID().toString())
                                    .sampleCount(0)
                                    .build()));

            // Xóa ảnh cũ
            cleanUpOldSamples(faceProfile);
            faceSampleRepository.deleteByFaceProfile(faceProfile);

            // Lưu ảnh mẫu mới
            int sampleCount = 0;
            String lastImagePath = null;

            if (request.getSamples() != null && !request.getSamples().isEmpty()) {
                for (int i = 0; i < request.getSamples().size(); i++) {
                    String imagePath = saveBase64Image(request.getSamples().get(i), user.getId(), i + 1);

                    faceSampleRepository.save(FaceSample.builder()
                            .faceProfile(faceProfile)
                            .imagePath(imagePath)
                            .sampleOrder(i + 1)
                            .build());
                    sampleCount++;
                    lastImagePath = imagePath;
                }
            }

            faceProfile.setSampleCount(sampleCount);
            faceProfile.setThumbnailUrl(lastImagePath);
            faceProfileRepository.save(faceProfile);

            return ResponseEntity.ok(FaceCaptureResponse.builder()
                    .faceCode(faceProfile.getFaceCode())
                    .thumbnailUrl(lastImagePath)
                    .message("Đăng ký thành công " + sampleCount + " mẫu khuôn mặt")
                    .success(true)
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(FaceCaptureResponse.builder().message(e.getMessage()).success(false).build());
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký face", e);
            return ResponseEntity.internalServerError()
                    .body(FaceCaptureResponse.builder().message("Lỗi máy chủ").success(false).build());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<LatestFaceResponse> getLatestFace() {
        try {
            User user = getCurrentUser();
            FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("Chưa đăng ký face"));

            return ResponseEntity.ok(LatestFaceResponse.builder()
                    .faceCode(faceProfile.getFaceCode())
                    .imagePath(faceProfile.getThumbnailUrl())
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy ảnh mới nhất", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-for-attendance")
    @Transactional // Bổ sung Transactional
    public ResponseEntity<FaceCaptureResponse> updateForAttendance(@RequestBody FaceCaptureRequest request) {
        try {
            User user = getCurrentUser();
            FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("Chưa đăng ký face"));

            // Xóa ảnh cũ
            String oldImagePath = faceProfile.getThumbnailUrl();
            if (oldImagePath != null) {
                deleteFileSafely(oldImagePath);
            }

            // Lưu ảnh mới
            String newImagePath = null;
            if (request.getSamples() != null && !request.getSamples().isEmpty()) {
                newImagePath = saveBase64Image(request.getSamples().get(0), user.getId(), 1);
            }

            faceProfile.setThumbnailUrl(newImagePath);
            faceProfileRepository.save(faceProfile);

            return ResponseEntity.ok(FaceCaptureResponse.builder()
                    .faceCode(faceProfile.getFaceCode())
                    .thumbnailUrl(newImagePath)
                    .message("Cập nhật ảnh điểm danh thành công")
                    .success(true)
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(FaceCaptureResponse.builder().message(e.getMessage()).success(false).build());
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật ảnh điểm danh", e);
            return ResponseEntity.internalServerError()
                    .body(FaceCaptureResponse.builder().message("Lỗi máy chủ").success(false).build());
        }
    }

    @DeleteMapping("/sample/{sampleId}")
    @Transactional // Bổ sung Transactional
    public ResponseEntity<Void> deleteFaceSample(@PathVariable Long sampleId) {
        try {
            User user = getCurrentUser();
            FaceSample sample = faceSampleRepository.findById(sampleId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu"));

            if (!sample.getFaceProfile().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Nên xóa DB trước, nếu thành công mới xóa file. Tránh trường hợp xóa file xong
            // DB lỗi (Rollback).
            faceSampleRepository.deleteById(sampleId);
            deleteFileSafely(sample.getImagePath());

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/attendance")
    public ResponseEntity<FaceIdAttendanceResponse> processFaceIdAttendance(
            @RequestBody FaceIdAttendanceRequest request) {

        // Ném thẳng request cho Service xử lý
        FaceIdAttendanceResponse response = faceIdAttendanceService.processAttendance(request);

        // Trả kết quả về cho AI (AI sẽ nhận được cái JSON response này)
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
    // --- Helper Methods ---

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
        } catch (IOException e) {
            log.warn("Không thể xóa file: {}", url);
        }
    }

    private String saveBase64Image(String base64Image, Long userId, int order) {
        try {
            String imageContent = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
            byte[] imageBytes = Base64.getDecoder().decode(imageContent);

            String fileName = "face_" + userId + "_" + order + ".jpg";
            String directory = "face_samples";
            Path targetPath = Paths.get(directory, String.valueOf(userId), fileName);

            storageProvider.save(targetPath, imageBytes);

            return "/uploads/" + directory + "/" + userId + "/" + fileName;
        } catch (Exception e) {
            log.error("Lỗi khi lưu ảnh base64", e);
            throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage());
        }
    }


    private String extractPathFromUrl(String url) {
        if (url == null || url.isBlank())
            return null;
        return url.startsWith("/uploads/") ? url.substring(9) : url;
    }
}