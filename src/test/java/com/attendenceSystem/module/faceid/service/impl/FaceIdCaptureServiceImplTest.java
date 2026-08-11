package com.attendenceSystem.module.faceid.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.attendenceSystem.module.faceid.dto.request.FaceCaptureRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceCaptureResponse;
import com.attendenceSystem.module.faceid.dto.response.LatestFaceResponse;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.entity.FaceSample;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.repository.FaceSampleRepository;
import com.attendenceSystem.module.faceid.service.FaceAiClient;
import com.attendenceSystem.module.faceid.service.FaceEmbeddingCacheService;
import com.attendenceSystem.module.storage.provider.StorageProvider;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class FaceIdCaptureServiceImplTest {

    @Mock
    private FaceAiClient faceAiClient;

    @Mock
    private FaceProfileRepository faceProfileRepository;

    @Mock
    private FaceSampleRepository faceSampleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageProvider storageProvider;

    @Mock
    private FaceEmbeddingCacheService faceEmbeddingCacheService;

    @InjectMocks
    private FaceIdCaptureServiceImpl service;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .username("HE001")
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .password("secret")
                .role(Role.EMPLOYEE)
                .status(Status.ACTIVE)
                .build();

        CustomUserDetails principal = CustomUserDetails.fromUser(currentUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerFaceCreatesProfileAndSavesValidSample() throws Exception {
        FaceAiClient.EmbedBatchResponse aiResponse = new FaceAiClient.EmbedBatchResponse();
        FaceAiClient.EmbedResult embedResult = new FaceAiClient.EmbedResult();
        embedResult.success = true;
        embedResult.embedding = "embedding";
        aiResponse.results = List.of(embedResult);

        when(userRepository.findByUsername("HE001")).thenReturn(Optional.of(currentUser));
        when(faceProfileRepository.findByUser(currentUser)).thenReturn(Optional.empty());
        when(faceProfileRepository.save(any(FaceProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(faceSampleRepository.findByFaceProfile(any(FaceProfile.class))).thenReturn(List.of());
        when(faceAiClient.embedBatch(any())).thenReturn(aiResponse);

        FaceCaptureResponse response = service.registerFace(FaceCaptureRequest.builder()
                .samples(List.of(dataUrl("sample")))
                .build());

        assertTrue(response.success());
        assertEquals("Đăng ký thành công 1 mẫu khuôn mặt", response.message());
        assertEquals("/uploads/face_samples/1/face_1_1.jpg", response.thumbnailUrl());

        ArgumentCaptor<FaceSample> sampleCaptor = ArgumentCaptor.forClass(FaceSample.class);
        verify(faceSampleRepository).save(sampleCaptor.capture());
        assertEquals(1, sampleCaptor.getValue().getSampleOrder());
        assertEquals("embedding", sampleCaptor.getValue().getEmbedding());
        assertEquals(response.thumbnailUrl(), sampleCaptor.getValue().getImagePath());

        verify(storageProvider).save(any(Path.class), any(byte[].class));
        verify(faceEmbeddingCacheService).invalidate();
    }

    @Test
    void registerFaceReturnsFailureWhenAllSamplesFail() throws Exception {
        FaceProfile faceProfile = FaceProfile.builder()
                .user(currentUser)
                .faceCode("face-code")
                .sampleCount(0)
                .build();
        FaceAiClient.EmbedBatchResponse aiResponse = new FaceAiClient.EmbedBatchResponse();
        FaceAiClient.EmbedResult failedResult = new FaceAiClient.EmbedResult();
        failedResult.success = false;
        failedResult.message = "Không thấy mặt";
        aiResponse.results = List.of(failedResult);

        when(userRepository.findByUsername("HE001")).thenReturn(Optional.of(currentUser));
        when(faceProfileRepository.findByUser(currentUser)).thenReturn(Optional.of(faceProfile));
        when(faceSampleRepository.findByFaceProfile(faceProfile)).thenReturn(List.of());
        when(faceAiClient.embedBatch(any())).thenReturn(aiResponse);

        FaceCaptureResponse response = service.registerFace(FaceCaptureRequest.builder()
                .samples(List.of(dataUrl("bad-sample")))
                .build());

        assertFalse(response.success());
        assertEquals("Không nhận diện được khuôn mặt hợp lệ nào trong các mẫu đã chụp", response.message());
        verify(faceSampleRepository, never()).save(any());
        verify(storageProvider, never()).save(any(Path.class), any(byte[].class));
        verify(faceEmbeddingCacheService, never()).invalidate();
    }

    @Test
    void getLatestFaceReturnsCurrentUserProfile() {
        FaceProfile faceProfile = FaceProfile.builder()
                .user(currentUser)
                .faceCode("face-code")
                .thumbnailUrl("/uploads/face_samples/1/face_1_1.jpg")
                .isAccept(Boolean.TRUE)
                .sampleCount(1)
                .build();

        when(userRepository.findByUsername("HE001")).thenReturn(Optional.of(currentUser));
        when(faceProfileRepository.findByUser(currentUser)).thenReturn(Optional.of(faceProfile));

        LatestFaceResponse response = service.getLatestFace();

        assertEquals("face-code", response.faceCode());
        assertEquals("/uploads/face_samples/1/face_1_1.jpg", response.imagePath());
        assertEquals("HE001", response.username());
        assertEquals("Nguyen Van A", response.fullName());
        assertEquals("a@example.com", response.email());
        assertEquals(Boolean.TRUE, response.isAccept());
    }

    @Test
    void updateForAttendanceReplacesThumbnailAndInvalidatesCache() throws Exception {
        FaceProfile faceProfile = FaceProfile.builder()
                .user(currentUser)
                .faceCode("face-code")
                .thumbnailUrl("/uploads/face_samples/1/old.jpg")
                .isAccept(Boolean.TRUE)
                .sampleCount(1)
                .build();

        when(userRepository.findByUsername("HE001")).thenReturn(Optional.of(currentUser));
        when(faceProfileRepository.findByUser(currentUser)).thenReturn(Optional.of(faceProfile));
        when(faceProfileRepository.save(any(FaceProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FaceCaptureResponse response = service.updateForAttendance(FaceCaptureRequest.builder()
                .samples(List.of(dataUrl("new-sample")))
                .build());

        assertTrue(response.success());
        assertEquals("face-code", response.faceCode());
        assertEquals("/uploads/face_samples/1/face_1_1.jpg", response.thumbnailUrl());
        assertEquals(response.thumbnailUrl(), faceProfile.getThumbnailUrl());
        assertNull(faceProfile.getIsAccept());

        verify(storageProvider).delete("face_samples/1/old.jpg");
        verify(storageProvider).save(any(Path.class), any(byte[].class));
        verify(faceProfileRepository).save(faceProfile);
        verify(faceEmbeddingCacheService).invalidate();
    }

    @Test
    void deleteFaceSampleDeletesOwnSampleAndInvalidatesCache() {
        FaceProfile faceProfile = FaceProfile.builder()
                .user(currentUser)
                .faceCode("face-code")
                .sampleCount(1)
                .build();
        FaceSample sample = FaceSample.builder()
                .id(10L)
                .faceProfile(faceProfile)
                .imagePath("/uploads/face_samples/1/face_1_1.jpg")
                .build();

        when(userRepository.findByUsername("HE001")).thenReturn(Optional.of(currentUser));
        when(faceSampleRepository.findById(10L)).thenReturn(Optional.of(sample));

        service.deleteFaceSample(10L);

        verify(faceSampleRepository).deleteById(10L);
        verify(storageProvider).delete("face_samples/1/face_1_1.jpg");
        verify(faceEmbeddingCacheService).invalidate();
    }

    @Test
    void deleteFaceSampleRejectsOtherUsersSample() {
        User owner = User.builder()
                .id(2L)
                .username("HE002")
                .build();
        FaceProfile faceProfile = FaceProfile.builder()
                .user(owner)
                .faceCode("face-code")
                .sampleCount(1)
                .build();
        FaceSample sample = FaceSample.builder()
                .id(10L)
                .faceProfile(faceProfile)
                .imagePath("/uploads/face_samples/2/face_2_1.jpg")
                .build();

        when(userRepository.findByUsername("HE001")).thenReturn(Optional.of(currentUser));
        when(faceSampleRepository.findById(10L)).thenReturn(Optional.of(sample));

        assertThrows(AccessDeniedException.class, () -> service.deleteFaceSample(10L));

        verify(faceSampleRepository, never()).deleteById(10L);
        verify(storageProvider, never()).delete(anyString());
        verify(faceEmbeddingCacheService, never()).invalidate();
    }

    private static String dataUrl(String content) {
        return "data:image/jpeg;base64," + Base64.getEncoder()
                .encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }
}
