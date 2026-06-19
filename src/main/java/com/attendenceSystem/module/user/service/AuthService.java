package com.attendenceSystem.module.user.service;

import com.attendenceSystem.module.user.dto.request.LoginRequest;
import com.attendenceSystem.module.user.dto.request.RegisterRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;

public interface AuthService {
    UserResponse login(LoginRequest request);

    void register(RegisterRequest request);

}
