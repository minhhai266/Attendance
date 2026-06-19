package com.attendenceSystem.module.log.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.log.dto.request.AuditLogRequest;
import com.attendenceSystem.module.log.dto.response.AuditLogResponse;

public interface AuditLogService {
    void log(AuditLogRequest request);

    Page<AuditLogResponse> getLogs(Pageable pageable);
}
