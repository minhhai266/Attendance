package com.attendenceSystem.module.report.enitty;

import java.time.Instant;

import com.attendenceSystem.module.report.enitty.enums.ReportStatus;
import com.attendenceSystem.module.user.entity.User;

import jakarta.persistence.Entity;

// @Entity
public class Report {

    private Long id;

    private User employee;

    private String content;

    private String attachmentUrl;

    private ReportStatus status;

    private String rejectReason;

    private Instant createdAt;

    private Instant reviewedAt;
}
