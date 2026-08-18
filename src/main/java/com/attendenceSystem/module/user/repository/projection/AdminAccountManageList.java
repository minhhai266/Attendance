package com.attendenceSystem.module.user.repository.projection;

import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
public interface AdminAccountManageList {
    Long getId();
    String getFullName();
    String getEmail();
    Role getRole();
    Status getStatus();
}
