package com.attendenceSystem.module.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.otp.dto.request.VerifyOtpRequest;
import com.attendenceSystem.module.user.dto.request.LoginRequest;
import com.attendenceSystem.module.user.dto.request.RegisterRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Routes.API + Routes.Auth.ROOT)
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping(Routes.Auth.LOGIN)
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        UserResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Routes.Auth.REGISTER)
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }
    @PostMapping(Routes.Auth.LOGOUT)
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }
    @PostMapping(Routes.Auth.FORGOT_PASSWORD)
    public ResponseEntity<Void> forgotPassword(@RequestBody String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }
    @PostMapping(Routes.Auth.VERIFY_OTP)
    public ResponseEntity<Boolean> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean isValid = authService.verifyOtp(request.getDestination(), request.getCode());
        return ResponseEntity.ok(isValid);
    }
    
}
