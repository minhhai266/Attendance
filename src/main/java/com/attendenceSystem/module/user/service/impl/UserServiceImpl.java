package com.attendenceSystem.module.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.attendenceSystem.exception.custom.BadRequestException;
import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.request.UpdateUserInformationRequest;
import com.attendenceSystem.module.user.dto.response.UserInformationResponse;
import com.attendenceSystem.module.user.entity.User;

import com.attendenceSystem.module.user.mapper.response.UserInformationResponseMapper;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.module.user.service.UserService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserInformationResponseMapper userInformationResponseMapper;

    @Transactional
    @Override
    public void changePassword(final ChangePasswordRequest request) {
        User currentUser = findCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        updatePassword(currentUser, request.getRequest());
    }

    @Transactional
    @Override
    public void updatePassword(final User user, final UpdatePasswordRequest request) {
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updatePasswordWithOtp(final UpdatePasswordWithOtpRequest request) {
        User user = userRepository.findUserByUsernameOrEmail(request.getDestination(), request.getDestination())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng với email: " + request.getDestination()));

        updatePassword(user, request.getRequest());
    }

    @Transactional
    @Override
    public UserInformationResponse updateUserInformation(final UpdateUserInformationRequest request) {
        User currentUser = findCurrentUser();
        if (StringUtils.hasText(request.getFullName())) {
            currentUser.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            if(!request.getPhone().equals(currentUser.getPhone())
                    && userRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Số điện thoại đã được sử dụng bởi người dùng khác!");
            }
            currentUser.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            if (!request.getEmail().equals(currentUser.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email đã được sử dụng bởi người dùng khác!");
            }
            currentUser.setEmail(request.getEmail());
        }
        if (request.getDepartment() != null) {
            currentUser.setDepartment(request.getDepartment());
        }
        userRepository.save(currentUser);
        return userInformationResponseMapper.fromEntity(currentUser);
    }

    private User findCurrentUser() {
        if (!SecurityUtil.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String currentUsername = SecurityUtil.getCurrentUserName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng với tên đăng nhập: " + currentUsername));
    }
}
