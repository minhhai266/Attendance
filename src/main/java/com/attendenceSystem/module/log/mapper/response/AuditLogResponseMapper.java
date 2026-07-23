package com.attendenceSystem.module.log.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.log.dto.response.AuditLogResponse;
import com.attendenceSystem.module.log.entity.AuditLog;

@Component
public class AuditLogResponseMapper {
        public AuditLogResponse fromEntity(AuditLog auditLog) {
                return AuditLogResponse.builder()
                                .id(auditLog.getId())
                                .userId(auditLog.getUser().getId())
                                .username(auditLog.getUser().getUsername())
                                .entityType(auditLog.getEntityType())
                                .entityId(auditLog.getEntityId())
                                .action(auditLog.getAction())
                                .description(auditLog.getDescription())
                                .createdAt(auditLog.getCreatedAt())
                                .build();
        }
}
