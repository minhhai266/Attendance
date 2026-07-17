package com.attendenceSystem.module.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.mapper.response.UserResponseMapper;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.module.user.service.UserService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    @Override
    public UserResponse getById(Long id) {
        return userResponseMapper.fromEntity(findById(id));
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = findById(id);
        validateAdminAction(user);
        userRepository.delete(user);
    }

    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userResponseMapper::fromEntity);
    }

    @Transactional
    @Override
    public void deactiveUser(Long id) {
        User user = findById(id);
        validateAdminAction(user);
        if (user.getStatus() == Status.INACTIVE) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }
        user.setStatus(Status.INACTIVE);
    }

    @Transactional
    @Override
    public void activateUser(Long id) {
        User user = findById(id);
        validateAdminAction(user);
        if (user.getStatus() == Status.ACTIVE) {
            throw new IllegalStateException("Tài khoản chưa bị khóa");
        }
        user.setStatus(Status.ACTIVE);
    }

    private void validateAdminAction(User targetUser) {
        String currentUsername = SecurityUtil.getCurrentUserName();
        Role currentRole = SecurityUtil.getCurrentUserRole();
        if (currentUsername == null || currentRole == null) {
            throw new IllegalStateException("Không xác định được tài khoản hiện tại");
        }
        if (targetUser.getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Admin không thể thay đổi tài khoản của chính mình");
        }
        if (currentRole == Role.ADMIN && targetUser.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Admin không thể tác động tài khoản admin khác");
        }
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng với tên đăng nhập: " + currentUsername));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        updatePassword(user, request.getRequest());
    }

    @Transactional
    @Override
    public void updatePassword(User user, UpdatePasswordRequest request) {
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updatePasswordWithOtp(UpdatePasswordWithOtpRequest request) {
        User user = userRepository.findUserByUsernameOrEmail(request.getDestination(), request.getDestination())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + request.getDestination()));

        updatePassword(user, request.getRequest());
    }
}