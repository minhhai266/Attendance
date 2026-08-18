package com.attendenceSystem.module.report.repository.projection;

import java.time.LocalDateTime;

public interface ReportList {
    String getEmployeeFullName();

    String getTitle();

    LocalDateTime getCreatedAt();

    String getAttachmentFiles();

    String getAttachmentUrl();

}
