package com.attendenceSystem.module.user.repository.projection;

import com.attendenceSystem.module.user.entity.enums.Role;

public interface ManagerAccountManageList {
    String getFullName();
    String getEmail();
    Role getRole();
}
