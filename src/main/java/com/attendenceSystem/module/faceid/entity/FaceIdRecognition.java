package com.attendenceSystem.module.faceid.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.attendenceSystem.module.faceid.entity.enums.FaceIdAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "face_id_recognition_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceIdRecognition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_code", nullable = false, length = 50)
    private String studentCode;

    /** Null khi là bản ghi điểm danh thủ công (không qua AI). */
    @Column(name = "confidence")
    private Double confidence;

    /** true nếu đây là điểm danh thủ công (offline, chọn tên thay vì AI nhận diện). */
    @Builder.Default
    @Column(name = "manual", nullable = false)
    private Boolean manual = false;

    @Column(name = "camera_id", length = 50)
    private String cameraId;

    @Column(name = "image_id", length = 255)
    private String imageId;

    @Column(name = "tracking_id", length = 255, unique = true)
    private String trackingId;

    @Column(name = "liveness")
    private Boolean liveness;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private FaceIdAction action;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "request_payload", columnDefinition = "JSON")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "JSON")
    private String responsePayload;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}