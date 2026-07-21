package com.attendenceSystem.module.log.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.log.dto.request.AuditLogRequest;
import com.attendenceSystem.module.log.dto.response.AuditLogResponse;
import com.attendenceSystem.module.log.entity.AuditLog;
import com.attendenceSystem.module.log.mapper.request.AuditLogRequestMapper;
import com.attendenceSystem.module.log.mapper.response.AuditLogResponseMapper;
import com.attendenceSystem.module.log.repository.AuditLogRepository;
import com.attendenceSystem.module.log.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogResponseMapper auditLogResponseMapper;

    @Transactional
    @Override
    public void log(final AuditLogRequest request) {
        AuditLog auditLog = AuditLogRequestMapper.toEntity(request);
        auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLogResponse> getLogs(final Pageable pageable) {
        return auditLogRepository
                .findAll(pageable)
                .map(auditLogResponseMapper::fromEntity);
    }

}
