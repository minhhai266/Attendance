package com.attendenceSystem.module.report.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.attendenceSystem.module.report.entity.enums.ReportStatus;

public record ReportDetailResponse(
        Long id,
        String title,
        String content,
        String attachmentUrl,
        String attachmentFiles,
        String employeeName,
        ReportStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        List<String> sharedUserNames) {
}
