package com.attendenceSystem.module.system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "recognition_threshold", nullable = false)
    private Double recognitionThreshold;

    @Column(name = "cooldown_seconds", nullable = false)
    private Integer cooldownSeconds;

    @Column(name = "frame_skip", nullable = false)
    private Integer frameSkip;

    @Column(name = "anti_spoofing_enabled", nullable = false)
    private Boolean antiSpoofingEnabled;

    @Column(name = "check_in_camera_source", length = 500)
    private String checkInCameraSource;

    @Column(name = "check_out_camera_source", length = 500)
    private String checkOutCameraSource;
}