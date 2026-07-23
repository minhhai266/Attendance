package com.attendenceSystem.module.faceid.entity;

import com.attendenceSystem.module.faceid.dto.FaceIdAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @Column(name = "confidence", nullable = false)
    private Double confidence;

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