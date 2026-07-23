package com.attendenceSystem.module.report.mapper.response;

import com.attendenceSystem.module.report.dto.response.ReportDetailResponse;
import com.attendenceSystem.module.report.entity.Report;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportDetailResponseMapper {

    public static ReportDetailResponse fromEntity(Report report) {
        return new ReportDetailResponse(
                report.getId(),
                report.getTitle(),
                report.getContent(),
                report.getAttachmentUrl(),
                report.getAttachmentFiles(),
                report.getEmployee().getFullName(),
                report.getStatus(),
                report.getRejectReason(),
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }
}