package com.attendenceSystem.module.report.dto.response;

import java.time.LocalDateTime;

import com.attendenceSystem.module.report.entity.enums.ReportStatus;

public record ReportResponse(
        Long id,
        String title,
        String content,
        String employeeName,
        ReportStatus status,
        LocalDateTime createdAt,
        String attachmentUrl,
        String attachmentFiles) {
}
