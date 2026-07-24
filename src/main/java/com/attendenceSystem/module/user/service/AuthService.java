package com.attendenceSystem.module.user.service;

import com.attendenceSystem.module.user.dto.request.LoginRequest;
import com.attendenceSystem.module.user.dto.request.RegisterRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;

public interface AuthService {
    UserResponse login(LoginRequest request);
    void logout();
    void register(RegisterRequest request);
    void forgotPassword(String email);
    boolean verifyOtp(String destination, String code);
}
