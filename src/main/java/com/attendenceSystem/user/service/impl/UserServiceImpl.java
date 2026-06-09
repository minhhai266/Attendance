package com.attendenceSystem.user.service.impl;

import java.time.Instant;
import java.time.ZoneId;

import com.attendenceSystem.user.dto.request.LoginRequest;
import com.attendenceSystem.user.dto.request.RegisterRequest;
import com.attendenceSystem.user.dto.response.UserResponse;
import com.attendenceSystem.user.entity.User;
import com.attendenceSystem.user.repository.UserRepository;
import com.attendenceSystem.user.service.UserService;
import com.attendenceSystem.util.TimeUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ZoneId applicationZoneId;
    // private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse login(LoginRequest request) {
        // Tìm tài khoản thông qua email từ request, không có báo lỗi.

        // Kiểm tra mật khẩu từ request có chính xác không. Sai báo lỗi

        // Trả UserResponse
        return null;
    }

    // @Transactional
    @Override
    public void register(RegisterRequest request) {
        // Kiểm tra email

        // encode password

        // Lưu User
    }

    @Override
    public void deleteUser(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public UserResponse findAccountById(Long id) {
        // Kiểm tra id == null

        // Tìm kiếm user thông qua id

        // Trả về UserResponse.
        return null;
    }

    private String getCurrentTime() {

        return TimeUtil.formatInstant(
                Instant.now(),
                applicationZoneId);
    }

}
