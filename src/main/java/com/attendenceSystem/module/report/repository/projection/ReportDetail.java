package com.attendenceSystem.module.report.repository.projection;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportDetail {
    String getTitle();
    String getEmployeeFullName();
    LocalDateTime getCreatedAt();
    String getContent();
    List<ReportShareInfo> getShares();
    String getAttachmentFiles();
    String getAttachmentUrl();
}
