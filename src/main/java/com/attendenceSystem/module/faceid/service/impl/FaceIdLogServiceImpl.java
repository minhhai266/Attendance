package com.attendenceSystem.module.faceid.service.impl;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.faceid.dto.FaceIdAction;
import com.attendenceSystem.module.faceid.dto.request.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceIdAttendanceResponse;
import com.attendenceSystem.module.faceid.entity.FaceIdRecognition;
import com.attendenceSystem.module.faceid.repository.FaceIdRecognitionRepository;
import com.attendenceSystem.module.faceid.service.FaceIdLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class FaceIdLogServiceImpl implements FaceIdLogService {
    private final FaceIdRecognitionRepository faceIdRecognitionRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void saveRecognitionLog(FaceIdAttendanceRequest request, FaceIdAction action, String message,
            AttendanceResponse attendance, LocalDateTime timestamp) {
        try {
            String requestPayload = objectMapper.writeValueAsString(request);
            String responsePayload = objectMapper.writeValueAsString(
                    FaceIdAttendanceResponse.builder()
                            .action(action)
                            .message(message)
                            .timestamp(timestamp)
                            .attendance(attendance)
                            .build());

            FaceIdRecognition logEntity = FaceIdRecognition.builder()
                    .studentCode(request.getStudentCode())
                    .confidence(request.getConfidence())
                    .cameraId(request.getCameraId())
                    .imageId(request.getImageId())
                    .trackingId(request.getTrackingId())
                    .liveness(request.getLiveness())
                    .action(action)
                    .message(message)
                    .requestPayload(requestPayload)
                    .responsePayload(responsePayload)
                    .capturedAt(request.getCapturedAt())
                    .build();

            faceIdRecognitionRepository.save(logEntity);
            log.info("Lưu FaceID log cho nhân viên: {}", request.getStudentCode());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request/response for log", e);
        } catch (Exception e) {
            log.error("Error saving FaceID log to database", e);
        }
    }

}
