package com.attendenceSystem.user.service;

import com.attendenceSystem.user.dto.request.LoginRequest;
import com.attendenceSystem.user.dto.request.RegisterRequest;
import com.attendenceSystem.user.dto.response.UserResponse;

public interface UserService {
    UserResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void deleteUser(Long id);

    UserResponse findAccountById(Long id);
}
