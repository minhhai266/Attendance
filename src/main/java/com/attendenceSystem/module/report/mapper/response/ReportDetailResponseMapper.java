package com.attendenceSystem.module.report.mapper.response;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.attendenceSystem.module.report.dto.response.ReportDetailResponse;
import com.attendenceSystem.module.report.entity.Report;
import com.attendenceSystem.module.report.entity.ReportShare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportDetailResponseMapper {

    public static ReportDetailResponse fromEntity(Report report) {
        List<String> sharedUserNames = Collections.emptyList();
        if (report.getShares() != null) {
            sharedUserNames = report.getShares().stream()
                    .map(ReportShare::getUser)
                    .map(user -> user.getFullName())
                    .collect(Collectors.toList());
        }

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
                report.getReviewedAt(),
                sharedUserNames
        );
    }
}
