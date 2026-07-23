package com.attendenceSystem.module.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.mapper.response.UserResponseMapper;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.module.user.service.AccountService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    @Override
    public Page<UserResponse> getUsers(final Pageable pageable) {
        return userRepository.findAll(pageable).map(userResponseMapper::fromEntity);
    }

    @Override
    public Page<UserResponse> getEmployees(final Pageable pageable) {
        return userRepository.findByRole(Role.EMPLOYEE, pageable)
                .map(userResponseMapper::fromEntity);
    }

    @Override
    public UserResponse getUserById(final Long id) {
        User user = findById(id);
        return userResponseMapper.fromEntity(user);
    }

    @Transactional
    @Override
    public void deactivateUser(final Long id) {
        User user = findById(id);
        validateAdminAction(user);
        if (user.getStatus() == Status.INACTIVE) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }
        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void activateUser(final Long id) {
        User user = findById(id);
        validateAdminAction(user);
        if (user.getStatus() == Status.ACTIVE) {
            throw new IllegalStateException("Tài khoản chưa bị khóa");
        }
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void changeDepartment(final Long id, final String departmentCode) {
        User user = findById(id);
        
        // Chỉ manager mới có quyền chuyển phòng
        if (!SecurityUtil.isAuthenticated()) {
            throw new IllegalStateException("Không xác định được tài khoản hiện tại");
        }
        Role currentUserRole = SecurityUtil.getCurrentUserRole();
        if (currentUserRole != Role.MANAGER && currentUserRole != Role.ADMIN) {
            throw new IllegalStateException("Bạn không có quyền thực hiện hành động này");
        }
        
        // Chỉ thao tác trên employee
        if (user.getRole() != Role.EMPLOYEE) {
            throw new IllegalStateException("Chỉ có thể chuyển phòng cho nhân viên");
        }
        
        Department department = null;
        if (departmentCode != null && !departmentCode.isBlank()) {
            try {
                department = Department.valueOf(departmentCode);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Mã phòng không hợp lệ: " + departmentCode);
            }
        }
        
        user.setDepartment(department);
        userRepository.save(user);
    }

    private User findById(final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));
    }

    private void validateAdminAction(final User targetUser) {
        if (!SecurityUtil.isAuthenticated()) {
            throw new IllegalStateException("Không xác định được tài khoản hiện tại");
        }

        String currentUsername = SecurityUtil.getCurrentUserName();
        Role currentUserRole = SecurityUtil.getCurrentUserRole();

        // 1. Chỉ ADMIN mới có quyền deactivate/activate tài khoản
        if (currentUserRole != Role.ADMIN) {
            throw new IllegalStateException("Bạn không có quyền thực hiện hành động này");
        }

        // 2. Check không tự tác động lên chính mình
        if (targetUser.getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Không thể thay đổi tài khoản của chính mình");
        }

        // 3. Check Admin không được đụng Admin khác
        if (targetUser.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Không thể tác động tài khoản admin khác");
        }
    }
}