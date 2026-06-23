package com.attendenceSystem.module.log.mapper.request;

import com.attendenceSystem.module.log.dto.request.AuditLogRequest;
import com.attendenceSystem.module.log.entity.AuditLog;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditLogRequestMapper {
    public AuditLog toEntity(AuditLogRequest request){
       return AuditLog.builder()
                .user(request.getUser())
                .entityType(request.getLogEntityType())
                .entityId(request.getEntityId())
                .action(request.getAction())
                .description(request.getDescription())
                .build();
    }
}
