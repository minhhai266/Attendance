package com.attendenceSystem.module.report.dto.response;

import java.time.Instant;

import com.attendenceSystem.module.report.entity.enums.ReportStatus;

public record ReportResponse(
        Long id,
        String title,
        String content,
        String employeeName,
        ReportStatus status,
        Instant createdAt) {
}