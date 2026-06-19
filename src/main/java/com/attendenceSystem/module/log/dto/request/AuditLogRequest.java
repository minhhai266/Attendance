package com.attendenceSystem.module.log.dto.request;

import com.attendenceSystem.module.log.entity.enums.LogAction;
import com.attendenceSystem.module.log.entity.enums.LogEntityType;
import com.attendenceSystem.module.user.entity.User;

public record AuditLogRequest(
        User user,
        LogEntityType logEntityType,
        Long entityId,
        LogAction action,
        String description) {

}
