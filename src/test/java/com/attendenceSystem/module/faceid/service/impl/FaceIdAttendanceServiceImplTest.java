package com.attendenceSystem.module.faceid.service.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.service.AttendanceActionService;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.faceid.dto.response.FaceIdentifyResponse;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.repository.FaceSampleRepository;
import com.attendenceSystem.module.faceid.service.FaceAiClient;
import com.attendenceSystem.module.faceid.service.FaceEmbeddingCacheService;
import com.attendenceSystem.module.faceid.service.FaceIdLogService;
import com.attendenceSystem.module.user.repository.UserRepository;

class FaceIdAttendanceServiceImplTest {

    @org.junit.jupiter.api.Test
    void shouldRecognizeSameEmbeddingWithHighSimilarity() {
        createService();
        String embedding = encodeEmbedding(new float[]{1f, 0f, 0f});

        double similarity = FaceIdAttendanceServiceImpl.cosineSimilarity(embedding, embedding);

        assertTrue(similarity >= 0.99d);
    }

    @org.junit.jupiter.api.Test
    void shouldRejectDifferentEmbeddingBelowThreshold() {
        createService();
        String embeddingA = encodeEmbedding(new float[]{1f, 0f, 0f});
        String embeddingB = encodeEmbedding(new float[]{0f, 1f, 0f});

        double similarity = FaceIdAttendanceServiceImpl.cosineSimilarity(embeddingA, embeddingB);

        assertTrue(similarity < 0.1d);
    }

    @org.junit.jupiter.api.Test
    void identifyWhenImageMissingReturnsMissingImageMessage() {
        FaceIdAttendanceServiceImpl service = createService();

        FaceIdentifyResponse response = service.identify(null);

        assertEquals(false, response.isFaceDetected());
        assertEquals(false, response.isMatched());
        assertEquals("Thiếu ảnh", response.getMessage());
    }

    private static FaceIdAttendanceServiceImpl createService() {
        return new FaceIdAttendanceServiceImpl(
                mock(AttendanceService.class),
                mock(AttendanceActionService.class),
                mock(UserRepository.class),
                mock(FaceIdLogService.class),
                mock(LeaveRequestRepository.class),
                mock(AttendanceRecordRepository.class),
                mock(FaceProfileRepository.class),
                mock(FaceSampleRepository.class),
                mock(FaceAiClient.class),
                mock(FaceEmbeddingCacheService.class)
        );
    }

    private static String encodeEmbedding(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : values) {
            buffer.putFloat(value);
        }
        return Base64.getEncoder().encodeToString(buffer.array());
    }
}
