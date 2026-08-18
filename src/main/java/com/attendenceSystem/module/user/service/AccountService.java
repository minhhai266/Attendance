package com.attendenceSystem.module.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.user.dto.response.AdminAccountManageListResponse;
import com.attendenceSystem.module.user.dto.response.ManagerAccountManageListResponse;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.enums.Role;

public interface AccountService {
    Page<AdminAccountManageListResponse> getUsers(Pageable pageable);
    Page<ManagerAccountManageListResponse> getEmployees(Pageable pageable);
    UserResponse getUserById(Long id);
    void deactivateUser(Long id);
    void activateUser(Long id);
    void changeDepartment(Long id, String departmentCode);
    void changeRole(Long id, Role newRole);
}
