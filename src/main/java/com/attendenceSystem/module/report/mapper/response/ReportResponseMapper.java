package com.attendenceSystem.module.report.mapper.response;

import com.attendenceSystem.module.report.dto.response.ReportResponse;
import com.attendenceSystem.module.report.entity.Report;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportResponseMapper {

    public static ReportResponse fromEntity(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getTitle(),
                report.getContent(),
                report.getEmployee().getFullName(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}