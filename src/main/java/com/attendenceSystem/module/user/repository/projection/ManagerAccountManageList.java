package com.attendenceSystem.module.user.repository.projection;

import com.attendenceSystem.module.user.entity.enums.Specialization;

public interface ManagerAccountManageList {
    Long getId();
    String getFullName();
    String getUsername();
    Specialization getSpecialization();
}
