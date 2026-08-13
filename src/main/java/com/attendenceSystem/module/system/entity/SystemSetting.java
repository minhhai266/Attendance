package com.attendenceSystem.module.system.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Builder.Default
    private Long id = 1L;

    @Column(name = "system_email", nullable = true)
    private String email;

    @Column(name = "system_email_password", nullable = true)
    private String emailPassword;

    @Column(name = "start_work_time", nullable = true)
    private LocalTime startWorkTime;

    @Column(name = "end_work_time", nullable = true)
    private LocalTime endWorkTime;

    @Column(name = "max_checkin_time", nullable = true)
    private LocalTime maxCheckinTime;

    @Column(name = "min_checkout_time", nullable = true)
    private LocalTime minCheckoutTime;

    @Column(name = "auto_mark_absent_cron", nullable = true)
    private String autoMarkAbsentCron;

    @Column(name = "auto_handle_missing_checkout_cron", nullable = true)
    private String autoHandleMissingCheckoutCron;

    @Column(name = "leave_auto_reject_cron", nullable = true)
    private String leaveAutoRejectCron;

    @Column(name = "leave_blackout_minutes", nullable = true)
    private Integer leaveBlackoutMinutes;

    @Column(name = "anti_spoofing_enabled", nullable = false)
    @Builder.Default
    private Boolean antiSpoofingEnabled = false;

    @Column(name = "cooldown_seconds", nullable = false)
    @Builder.Default
    private Integer cooldownSeconds = 5;

    @Column(name = "frame_skip", nullable = false)
    @Builder.Default
    private Integer frameSkip = 3;

    @Column(name = "recognition_threshold", nullable = false)
    @Builder.Default
    private Double recognitionThreshold = 0.6;
}