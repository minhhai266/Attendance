package com.attendenceSystem.module.faceid.service.impl;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.faceid.dto.FaceIdAction;
import com.attendenceSystem.module.faceid.dto.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.FaceIdAttendanceResponse;
import com.attendenceSystem.module.faceid.entity.FaceIdRecognition;
import com.attendenceSystem.module.faceid.repository.FaceIdRecognitionRepository;
import com.attendenceSystem.module.faceid.service.FaceIdAttendanceService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceIdAttendanceServiceImpl implements FaceIdAttendanceService {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final FaceIdRecognitionRepository faceIdRecognitionRepository;

    @Value("${face-id.confidence-threshold:0.90}")
    private double confidenceThreshold;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Cache<String, FaceIdAction> processedTrackingIds = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(100_000)
            .build();

    @Override
    @Transactional
    public FaceIdAttendanceResponse processAttendance(FaceIdAttendanceRequest request) {
        String trackingId = request.getTrackingId();
        Instant timestamp = Instant.now();

        // 1. Check idempotency (only if trackingId is present)
        if (StringUtils.hasText(trackingId)) {
            FaceIdAction cachedAction = processedTrackingIds.getIfPresent(trackingId);
            if (cachedAction != null) {
                log.info("Ignoring duplicate trackingId: {}", trackingId);
                return buildResponse(FaceIdAction.IGNORED, "DUPLICATE", trackingId, timestamp, null);
            }
        }

        // 2. Check confidence threshold
        if (request.getConfidence() < confidenceThreshold) {
            String message = "Confidence below threshold: " + request.getConfidence();
            log.warn(message);
            saveRecognitionLog(request, FaceIdAction.FAILED, message, null, timestamp);
            return buildResponse(FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        // 3. Find user by username (studentCode)
        User user = userRepository.findByUsername(request.getStudentCode())
                .orElse(null);

        if (user == null) {
            String message = "User not found: " + request.getStudentCode();
            log.warn(message);
            saveRecognitionLog(request, FaceIdAction.FAILED, message, null, timestamp);
            return buildResponse(FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        // 4. Determine action using AttendanceService state query
        FaceIdAction action;
        AttendanceResponse attendanceResponse = null;
        String message = "Success";

        try {
            Optional<AttendanceRecord> todayRecord = attendanceService.getTodayAttendanceRecord(user);

            if (todayRecord.isEmpty()) {
                // Not checked in yet -> CHECKIN
                attendanceResponse = attendanceService.checkIn(user);
                action = FaceIdAction.CHECKIN;
                message = "Checked in successfully";
            } else if (todayRecord.get().getCheckOutTime() == null) {
                // Checked in, not checked out -> CHECKOUT
                attendanceResponse = attendanceService.checkOut(user);
                action = FaceIdAction.CHECKOUT;
                message = "Checked out successfully";
            } else {
                // Already checked out
                action = FaceIdAction.IGNORED;
                message = "Already checked out today";
            }

            log.info("Face ID attendance processed: student={}, action={}", request.getStudentCode(), action);

        } catch (Exception e) {
            // Catch specific attendance exceptions
            String errorMessage = e.getMessage();
            log.error("Attendance processing error for student {}: {}", request.getStudentCode(), errorMessage, e);
            saveRecognitionLog(request, FaceIdAction.FAILED, errorMessage, null, timestamp);
            return buildResponse(FaceIdAction.FAILED, errorMessage, trackingId, timestamp, null);
        }

        // 5. Save recognition log
        saveRecognitionLog(request, action, message, attendanceResponse, timestamp);

        // 6. Cache trackingId for idempotency
        if (org.springframework.util.StringUtils.hasText(trackingId)) {
            processedTrackingIds.put(trackingId, action);
        }

        // 7. Build and return response
        return buildResponse(true, action, message, trackingId, timestamp, attendanceResponse);
    }

    private void saveRecognitionLog(
            FaceIdAttendanceRequest request,
            FaceIdAction action,
            String message,
            AttendanceResponse attendance,
            Instant timestamp) {

        try {
            String requestPayload = objectMapper.writeValueAsString(request);
            String responsePayload = objectMapper.writeValueAsString(
                    FaceIdAttendanceResponse.builder()
                            .action(action)
                            .message(message)
                            .timestamp(timestamp)
                            .build()
            );

            FaceIdRecognition log = FaceIdRecognition.builder()
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

            faceIdRecognitionRepository.save(log);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request/response for log", e);
        }
    }

    private FaceIdAttendanceResponse buildResponse(
            FaceIdAction action,
            String message,
            String trackingId,
            Instant timestamp,
            AttendanceResponse attendance) {

        return FaceIdAttendanceResponse.builder()
                .success(action != FaceIdAction.FAILED)
                .action(action)
                .message(message)
                .trackingId(trackingId)
                .timestamp(timestamp)
                .attendance(attendance)
                .build();
    }

    private FaceIdAttendanceResponse buildResponse(
            boolean success,
            FaceIdAction action,
            String message,
            String trackingId,
            Instant timestamp,
            AttendanceResponse attendance) {

        return FaceIdAttendanceResponse.builder()
                .success(success)
                .action(action)
                .message(message)
                .trackingId(trackingId)
                .timestamp(timestamp)
                .attendance(attendance)
                .build();
    }
}