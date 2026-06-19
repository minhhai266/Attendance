package com.attendenceSystem.module.log.dto.request;

import com.attendenceSystem.module.log.entity.enums.LogAction;
import com.attendenceSystem.module.log.entity.enums.LogEntityType;
import com.attendenceSystem.module.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequest {
    private User user;
    private LogEntityType logEntityType;
    private Long entityId;
    private LogAction action;
    private String description;
}
