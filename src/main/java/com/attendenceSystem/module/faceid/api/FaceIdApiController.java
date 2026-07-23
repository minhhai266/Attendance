package com.attendenceSystem.module.faceid.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.faceid.dto.request.FaceCaptureRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceCaptureResponse;
import com.attendenceSystem.module.faceid.dto.response.LatestFaceResponse;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.entity.FaceSample;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.repository.FaceSampleRepository;
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

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public FaceCaptureResponse registerFace(@RequestBody FaceCaptureRequest request) {
        try {
            String currentUsername = SecurityUtil.getCurrentUserName();
            User user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            // Tìm FaceProfile hoặc tạo mới
            FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        String faceCode = UUID.randomUUID().toString();
                        FaceProfile newProfile = FaceProfile.builder()
                                .user(user)
                                .faceCode(faceCode)
                                .sampleCount(0)
                                .build();
                        // Lưu FaceProfile trước khi sử dụng để tránh TransientObjectException
                        return faceProfileRepository.save(newProfile);
                    });

            // Xóa các ảnh mẫu cũ nếu có
            List<FaceSample> oldSamples = faceSampleRepository.findByFaceProfile(faceProfile);
            for (FaceSample oldSample : oldSamples) {
                try {
                    String path = extractPathFromUrl(oldSample.getImagePath());
                    if (path != null) {
                        storageProvider.delete(path);
                    }
                } catch (IOException e) {
                    log.warn("Không thể xóa file cũ: {}", oldSample.getImagePath());
                }
            }
            faceSampleRepository.deleteByFaceProfile(faceProfile);

            // Lưu ảnh mẫu mới
            int sampleCount = 0;
            String lastImagePath = null;
            
            if (request.getSamples() != null && !request.getSamples().isEmpty()) {
                for (int i = 0; i < request.getSamples().size(); i++) {
                    String base64Image = request.getSamples().get(i);
                    String imagePath = saveBase64Image(base64Image, user.getId(), i + 1);
                    
                    FaceSample faceSample = FaceSample.builder()
                            .faceProfile(faceProfile)
                            .imagePath(imagePath)
                            .sampleOrder(i + 1)
                            .build();
                    faceSampleRepository.save(faceSample);
                    sampleCount++;
                    lastImagePath = imagePath;
                }
            }

            // Cập nhật FaceProfile
            faceProfile.setSampleCount(sampleCount);
            faceProfile.setThumbnailUrl(lastImagePath);
            faceProfileRepository.save(faceProfile);

            return FaceCaptureResponse.builder()
                    .faceCode(faceProfile.getFaceCode())
                    .thumbnailUrl(lastImagePath)
                    .message("Đăng ký thành công " + sampleCount + " mẫu khuôn mặt")
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi đăng ký face", e);
            return FaceCaptureResponse.builder()
                    .message("Lỗi: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    @GetMapping("/latest")
    public LatestFaceResponse getLatestFace() {
        try {
            String currentUsername = SecurityUtil.getCurrentUserName();
            User user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("Chưa đăng ký face"));

            return LatestFaceResponse.builder()
                    .faceCode(faceProfile.getFaceCode())
                    .imagePath(faceProfile.getThumbnailUrl())
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi lấy ảnh mới nhất", e);
            return LatestFaceResponse.builder()
                    .faceCode(null)
                    .imagePath(null)
                    .build();
        }
    }

    @PostMapping("/update-for-attendance")
    @ResponseStatus(HttpStatus.OK)
    public FaceCaptureResponse updateForAttendance(@RequestBody FaceCaptureRequest request) {
        try {
            String currentUsername = SecurityUtil.getCurrentUserName();
            User user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            FaceProfile faceProfile = faceProfileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("Chưa đăng ký face"));

            // Xóa ảnh cũ nếu có
            String oldImagePath = faceProfile.getThumbnailUrl();
            if (oldImagePath != null) {
                try {
                    String path = extractPathFromUrl(oldImagePath);
                    if (path != null) {
                        storageProvider.delete(path);
                    }
                } catch (IOException e) {
                    log.warn("Không thể xóa ảnh cũ: {}", oldImagePath);
                }
            }

            // Lưu ảnh mới (chỉ lưu 1 ảnh cho điểm danh)
            String newImagePath = null;
            if (request.getSamples() != null && !request.getSamples().isEmpty()) {
                String base64Image = request.getSamples().get(0);
                newImagePath = saveBase64Image(base64Image, user.getId(), 1);
            }

            // Cập nhật thumbnailUrl
            faceProfile.setThumbnailUrl(newImagePath);
            faceProfileRepository.save(faceProfile);

            return FaceCaptureResponse.builder()
                    .faceCode(faceProfile.getFaceCode())
                    .thumbnailUrl(newImagePath)
                    .message("Cập nhật ảnh điểm danh thành công")
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật ảnh điểm danh", e);
            return FaceCaptureResponse.builder()
                    .message("Lỗi: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    @DeleteMapping("/sample/{sampleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFaceSample(@PathVariable Long sampleId) {
        try {
            String currentUsername = SecurityUtil.getCurrentUserName();
            User user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            FaceSample sample = faceSampleRepository.findById(sampleId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu"));

            // Kiểm tra quyền sở hữu
            if (!sample.getFaceProfile().getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Không có quyền xóa mẫu này");
            }

            // Xóa file
            String path = extractPathFromUrl(sample.getImagePath());
            if (path != null) {
                storageProvider.delete(path);
            }
            faceSampleRepository.deleteById(sampleId);

        } catch (IOException e) {
            log.error("Lỗi khi xóa file", e);
        }
    }

    private String saveBase64Image(String base64Image, Long userId, int order) {
        try {
            // Loại bỏ prefix data:image/jpeg;base64,
            String imageContent = base64Image;
            if (base64Image.contains(",")) {
                imageContent = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(imageContent);
            
            String fileName = "face_" + userId + "_" + order + ".jpg";
            String directory = "face_samples";
            String relativePath = directory + "/" + userId + "/" + fileName;
            
            // Sử dụng Paths.get với nhiều tham số để tránh vấn đề với đường dẫn tương đối
            Path targetPath = Paths.get(directory, String.valueOf(userId), fileName);
            
            // Tạo MultipartFile ảo
            MultipartFile multipartFile = createMultipartFile(fileName, imageBytes);
            
            // Lưu file
            storageProvider.save(targetPath, multipartFile);
            
            return "/uploads/" + relativePath;

        } catch (Exception e) {
            log.error("Lỗi khi lưu ảnh base64", e);
            throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage());
        }
    }

    private MultipartFile createMultipartFile(String fileName, byte[] bytes) {
        return new MultipartFile() {
            @Override
            public String getName() { return fileName; }
            @Override
            public String getOriginalFilename() { return fileName; }
            @Override
            public String getContentType() { return "image/jpeg"; }
            @Override
            public boolean isEmpty() { return bytes.length == 0; }
            @Override
            public long getSize() { return bytes.length; }
            @Override
            public byte[] getBytes() { return bytes; }
            @Override
            public ByteArrayInputStream getInputStream() { return new ByteArrayInputStream(bytes); }
            @Override
            public void transferTo(java.io.File dest) throws IOException { 
                Files.write(dest.toPath(), bytes); 
            }
        };
    }

    private String extractPathFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        if (url.startsWith("/uploads/")) {
            return url.substring(8); // Bỏ "/uploads/" prefix
        }
        return url;
    }
}