package com.attendenceSystem.module.report.dto.response;

import java.time.Instant;

import com.attendenceSystem.module.report.entity.enums.ReportStatus;

public record ReportDetailResponse(
        Long id,
        String title,
        String content,
        String attachmentUrl,
        String employeeName,
        ReportStatus status,
        String rejectReason,
        Instant createdAt,
        Instant reviewedAt) {
}