package com.attendenceSystem.module.faceid.service.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.service.AttendanceActionService;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.faceid.dto.FaceIdAction;
import com.attendenceSystem.module.faceid.dto.request.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceIdAttendanceResponse;
import com.attendenceSystem.module.faceid.dto.response.FaceIdentifyResponse;
import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.faceid.entity.FaceSample;
import com.attendenceSystem.module.faceid.repository.FaceProfileRepository;
import com.attendenceSystem.module.faceid.repository.FaceSampleRepository;
import com.attendenceSystem.module.faceid.service.FaceAiClient;
import com.attendenceSystem.module.faceid.service.FaceEmbeddingCacheService;
import com.attendenceSystem.module.faceid.service.FaceEmbeddingEntry;
import com.attendenceSystem.module.faceid.service.FaceIdAttendanceService;
import com.attendenceSystem.module.faceid.service.FaceIdLogService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceIdAttendanceServiceImpl implements FaceIdAttendanceService {

    private final AttendanceService attendanceService;
    private final AttendanceActionService attendanceActionService;
    private final UserRepository userRepository;
    private final FaceIdLogService faceIdLogService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final FaceProfileRepository faceProfileRepository;
    private final FaceSampleRepository faceSampleRepository;
    private final FaceAiClient faceAiClient;
    private final FaceEmbeddingCacheService faceEmbeddingCacheService;

    @Value("${face-id.confidence-threshold:0.55}")
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

        // 2. Find user by username (studentCode)
        User user = userRepository.findByUsername(request.getStudentCode())
                .orElse(null);

        if (user == null) {
            String message = "User not found: " + request.getStudentCode();
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        // 3. Verify the captured image against the user face samples using face-ai
        if (!StringUtils.hasText(request.getImageBase64())) {
            String message = "Thiếu ảnh chụp để nhận diện khuôn mặt.";
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        FaceProfile faceProfile = faceProfileRepository.findByUser(user).orElse(null);
        if (faceProfile == null) {
            String message = "Chưa có dữ liệu khuôn mặt đã đăng ký cho người dùng này.";
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        List<FaceSample> faceSamples = faceSampleRepository.findByFaceProfileOrderBySampleOrderAsc(faceProfile);
        if (faceSamples.isEmpty()) {
            String message = "Không tìm thấy mẫu khuôn mặt nào đã đăng ký.";
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        FaceAiClient.PoseResult poseResult = faceAiClient.checkPose(request.getImageBase64());
        if (poseResult == null || !Boolean.TRUE.equals(poseResult.ok)) {
            String message = poseResult != null && StringUtils.hasText(poseResult.message)
                    ? poseResult.message
                    : "Vui lòng nhìn thẳng vào camera và giữ khuôn mặt ở trung tâm.";
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        FaceAiClient.EmbedResult embedResult = faceAiClient.embedSingle(request.getImageBase64());
        if (embedResult == null || !embedResult.success || !StringUtils.hasText(embedResult.embedding)) {
            String message = embedResult != null && StringUtils.hasText(embedResult.message)
                    ? embedResult.message
                    : "Không thể tạo embedding từ ảnh chụp.";
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        double bestSimilarity = 0.0;
        for (FaceSample faceSample : faceSamples) {
            if (!StringUtils.hasText(faceSample.getEmbedding())) {
                continue;
            }
            double similarity = cosineSimilarity(embedResult.embedding, faceSample.getEmbedding());
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
            }
        }

        if (bestSimilarity < confidenceThreshold) {
            String message = "Không khớp khuôn mặt với dữ liệu đăng ký. Độ tương đồng: " + String.format("%.3f", bestSimilarity);
            log.warn(message);
            return buildResponseAndLog(request, FaceIdAction.FAILED, message, trackingId, timestamp, null);
        }

        // 4. Determine action using AttendanceService state query
        FaceIdAction action;
        AttendanceResponse attendanceResponse = null;
        String responseMessage;

        try {
            Optional<AttendanceRecord> todayRecord = attendanceService.getTodayAttendanceRecord(user);

            if (todayRecord.isEmpty()) {
                // Not checked in yet -> CHECKIN
                attendanceResponse = attendanceActionService.checkIn(user);
                action = FaceIdAction.CHECKIN;
                responseMessage = "Checked in successfully";
            } else if (todayRecord.get().getCheckOutTime() == null) {
                // Checked in, not checked out -> CHECKOUT
                attendanceResponse = attendanceActionService.checkOut(user);
                action = FaceIdAction.CHECKOUT;
                responseMessage = "Checked out successfully";
            } else {
                // Already checked out
                action = FaceIdAction.IGNORED;
                responseMessage = "Already checked out today";
            }

            // Nếu user đang trong ngày nghỉ phép đã duyệt, ghi chú vào attendance record
            if (action == FaceIdAction.CHECKIN || action == FaceIdAction.CHECKOUT) {
                LocalDate today = LocalDate.now();
                List<Long> onLeaveUserIds = leaveRequestRepository.findUserIdsOnLeaveForDate(today, LeaveStatus.APPROVED);
                if (onLeaveUserIds.contains(user.getId())) {
                    Optional<AttendanceRecord> updatedRecord = attendanceRecordRepository.findByUserAndAttendanceDate(user, today);
                    updatedRecord.ifPresent(record -> {
                        String currentNote = record.getNote() != null ? record.getNote() + "; " : "";
                        record.setNote(currentNote + "Đi làm trong ngày nghỉ phép");
                        attendanceRecordRepository.save(record);
                    });
                }
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
        return buildResponseAndLog(request, action, responseMessage, trackingId, timestamp, attendanceResponse);
    }

    @Override
    public FaceIdentifyResponse identify(String imageBase64) {
        if (!StringUtils.hasText(imageBase64)) {
            return FaceIdentifyResponse.builder()
                    .faceDetected(false)
                    .matched(false)
                    .message("Thiếu ảnh")
                    .build();
        }

        FaceAiClient.PoseResult pose = faceAiClient.checkPose(imageBase64);
        if (pose == null || !Boolean.TRUE.equals(pose.ok)) {
            return FaceIdentifyResponse.builder()
                    .faceDetected(pose != null && pose.face_count > 0)
                    .bbox(pose != null ? pose.bbox : null)
                    .pose(pose != null ? pose.pose : null)
                    .matched(false)
                    .message(pose != null ? pose.message : "Không phát hiện khuôn mặt")
                    .build();
        }

        FaceAiClient.EmbedResult embedResult = faceAiClient.embedSingle(imageBase64);
        if (embedResult == null || !embedResult.success || !StringUtils.hasText(embedResult.embedding)) {
            return FaceIdentifyResponse.builder()
                    .faceDetected(true)
                    .bbox(pose.bbox)
                    .pose(pose.pose)
                    .matched(false)
                    .message("Không tạo được embedding")
                    .build();
        }

        float[] queryVector = decodeQueryEmbedding(embedResult.embedding);
        if (queryVector == null) {
            return FaceIdentifyResponse.builder()
                    .faceDetected(true)
                    .bbox(pose.bbox)
                    .pose(pose.pose)
                    .matched(false)
                    .message("Embedding không hợp lệ")
                    .build();
        }

        List<FaceEmbeddingEntry> knownFaces = faceEmbeddingCacheService.getKnownEmbeddings();
        FaceEmbeddingEntry best = null;
        double bestSimilarity = 0.0;
        for (FaceEmbeddingEntry entry : knownFaces) {
            double similarity = FaceEmbeddingCacheService.cosineSimilarity(queryVector, entry.getVector());
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                best = entry;
            }
        }

        boolean matched = best != null && bestSimilarity >= confidenceThreshold;
        FaceEmbeddingEntry matchedEntry = matched ? best : null;
        return FaceIdentifyResponse.builder()
                .faceDetected(true)
                .bbox(pose.bbox)
                .pose(pose.pose)
                .matched(matched)
                .studentCode(matchedEntry != null ? matchedEntry.getStudentCode() : null)
                .fullName(matchedEntry != null ? matchedEntry.getFullName() : null)
                .confidence(Math.round(bestSimilarity * 10000.0) / 10000.0)
                .message(matched ? "Nhận diện thành công" : "Chưa khớp với dữ liệu đã đăng ký")
                .build();
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

    
    public static double cosineSimilarity(String leftEmbedding, String rightEmbedding) {
        if (!StringUtils.hasText(leftEmbedding) || !StringUtils.hasText(rightEmbedding)) {
            return 0.0d;
        }

        try {
            float[] left = decodeEmbedding(leftEmbedding);
            float[] right = decodeEmbedding(rightEmbedding);
            if (left.length != right.length) {
                return 0.0d;
            }

            double dotProduct = 0.0d;
            double leftNorm = 0.0d;
            double rightNorm = 0.0d;
            for (int i = 0; i < left.length; i++) {
                dotProduct += left[i] * right[i];
                leftNorm += left[i] * left[i];
                rightNorm += right[i] * right[i];
            }

            double denominator = Math.sqrt(leftNorm) * Math.sqrt(rightNorm);
            if (denominator == 0.0d) {
                return 0.0d;
            }
            return dotProduct / denominator;
        } catch (IllegalArgumentException ex) {
            return 0.0d;
        }
    }

    private static float[] decodeEmbedding(String embedding) {
        byte[] bytes = Base64.getDecoder().decode(embedding);
        if (bytes.length % Float.BYTES != 0) {
            throw new IllegalArgumentException("Invalid embedding length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] values = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.getFloat();
        }
        return values;
    }

    private float[] decodeQueryEmbedding(String base64Embedding) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64Embedding);
            if (bytes.length % Float.BYTES != 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            float[] values = new float[bytes.length / Float.BYTES];
            for (int i = 0; i < values.length; i++) {
                values[i] = buffer.getFloat();
            }
            return values;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    
}