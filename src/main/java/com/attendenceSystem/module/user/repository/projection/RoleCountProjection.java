package com.attendenceSystem.module.user.repository.projection;

import com.attendenceSystem.module.user.entity.enums.Role;

public interface RoleCountProjection {
    Role getRole();

    Long getCount();

}