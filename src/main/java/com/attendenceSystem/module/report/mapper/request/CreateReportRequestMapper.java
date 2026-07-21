package com.attendenceSystem.module.report.mapper.request;

import com.attendenceSystem.module.report.dto.request.CreateReportRequest;
import com.attendenceSystem.module.report.entity.Report;
import com.attendenceSystem.module.report.entity.enums.ReportStatus;
import com.attendenceSystem.module.user.entity.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateReportRequestMapper {

    public static Report toEntity(CreateReportRequest request, User employee) {
        return Report.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .employee(employee)
                .status(ReportStatus.PENDING)
                .build();
    }
}