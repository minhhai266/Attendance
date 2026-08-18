package com.attendenceSystem.module.user.repository.projection;

import com.attendenceSystem.module.user.entity.enums.Role;

public interface EmployeeDetail {
    String getFullName();
    String getEmail();
    String getPhone();
    Role getRole();
}
