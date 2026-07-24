package com.attendenceSystem.module.faceid.service.impl;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.faceid.dto.request.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceIdAttendanceResponse;
import com.attendenceSystem.module.faceid.entity.enums.FaceIdAction;
import com.attendenceSystem.module.faceid.service.FaceIdAttendanceService;
import com.attendenceSystem.module.faceid.service.FaceIdLogService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceIdAttendanceServiceImpl implements FaceIdAttendanceService {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final FaceIdLogService faceIdLogService;

    @Value("${face-id.confidence-threshold:0.90}")
    private double confidenceThreshold;

    private final Cache<String, FaceIdAction> processedTrackingIds = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(100_00)
            .build();
            
    @Transactional
    @Override
    public FaceIdAttendanceResponse processAttendance(FaceIdAttendanceRequest request) {
        String trackingId = request.getTrackingId();
        LocalDateTime timestamp = LocalDateTime.now();

        // 1. Check idempotency (only if trackingId is present)
        if (StringUtils.hasText(trackingId)) {
            FaceIdAction cachedAction = processedTrackingIds.getIfPresent(trackingId);
            if (cachedAction != null) {
                return buildResponseAndLog(request, FaceIdAction.IGNORED, "DUPLICATE", trackingId, timestamp, null);
            }
        }

        // 2. Check confidence threshold
        if (request.getConfidence() < confidenceThreshold) {
            String message = "Confidence below threshold: " + request.getConfidence();
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        // 3. Find user by username (studentCode)
        User user = userRepository.findByUsername(request.getStudentCode())
                .orElse(null);

        if (user == null) {
            String message = "User not found: " + request.getStudentCode();
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
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
            return buildResponseAndLog(request, FaceIdAction.FAILED, errorMessage, trackingId, timestamp, null);
        }

        // 5. Cache trackingId for idempotency
        if (StringUtils.hasText(trackingId)) {
            processedTrackingIds.put(trackingId, action);
        }

        // 6. Build and return response
        return buildResponseAndLog(request, action, message, trackingId, timestamp, attendanceResponse);
    }

    private FaceIdAttendanceResponse buildResponseAndLog(
            FaceIdAttendanceRequest request,
            FaceIdAction action,
            String message,
            String trackingId,
            LocalDateTime timestamp,
            AttendanceResponse attendance) {
        
                faceIdLogService.saveRecognitionLog(request, action, message, attendance, timestamp);

        return FaceIdAttendanceResponse.builder()
                .success(action != FaceIdAction.FAILED)
                .action(action)
                .message(message)
                .trackingId(trackingId)
                .timestamp(timestamp)
                .attendance(attendance)
                .build();
    }
}