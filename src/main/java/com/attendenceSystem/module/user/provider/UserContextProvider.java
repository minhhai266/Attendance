package com.attendenceSystem.module.user.provider;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.attendenceSystem.module.user.dto.response.UserSimpleResponse;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserContextProvider {
    private final UserRepository userRepository;

    public User getCurrentUserEntity() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy người dùng với tên đăng nhập: " + currentUserId));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy người dùng với id: " + id));
    }

    public List<User> getEmployeesByDepartment(String departmentId) {
        if (departmentId == null || departmentId.isEmpty()) {
            return userRepository.findByRoleNot(Role.ADMIN);
        }

        Department dept = Department.fromValue(departmentId);
        if (dept == null) {
            return userRepository.findByRoleNot(Role.ADMIN);
        }

        return userRepository.findByDepartmentAndRoleNot(dept, Role.ADMIN);
    }

    public List<UserSimpleResponse> getFullNamesByDepartmentAndRoleNotAdmin(String departmentId) {
        Department department = Department.fromValue(departmentId);
        if (department == null) {
            return Collections.emptyList();
        }
        return userRepository.findFullNameByDepartmentAndRoleNot(department, Role.ADMIN);
    }

    public List<User> getUsersWithoutAttendanceForDate(LocalDate date){
        return userRepository.findUsersWithoutAttendanceForDate(date, Status.ACTIVE);

    }
}
