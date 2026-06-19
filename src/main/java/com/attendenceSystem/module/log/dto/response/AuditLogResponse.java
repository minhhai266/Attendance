package com.attendenceSystem.module.log.dto.response;

import com.attendenceSystem.module.log.entity.enums.LogAction;
import com.attendenceSystem.module.log.entity.enums.LogEntityType;

import lombok.Builder;

@Builder
public record AuditLogResponse(
                Long id,
                Long userId,
                String username,
                LogEntityType entityType,
                Long entityId,
                LogAction action,
                String description,
                String createdAt) {
}
