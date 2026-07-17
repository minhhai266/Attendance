package com.attendenceSystem.module.user.service.impl;

import com.attendenceSystem.module.user.mapper.request.RegisterRequestMapper;
import com.attendenceSystem.module.user.mapper.response.UserResponseMapper;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;

import com.attendenceSystem.module.otp.dto.request.SendOtpRequest;
import com.attendenceSystem.module.otp.dto.request.VerifyOtpRequest;
import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;
import com.attendenceSystem.module.otp.exception.OtpExpiredException;
import com.attendenceSystem.module.otp.exception.OtpInvalidException;
import com.attendenceSystem.module.otp.exception.OtpNotFoundException;
import com.attendenceSystem.module.otp.service.OptService;
import com.attendenceSystem.module.user.dto.request.LoginRequest;
import com.attendenceSystem.module.user.dto.request.RegisterRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.module.user.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserResponseMapper userResponseMapper;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final OptService otpService;

    @Transactional
    @Override
    public void register(RegisterRequest request) {
        if (existsByKeyword(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }
        if (existsByKeyword(request.getEmail())) {
            throw new IllegalArgumentException("Email này đã được đăng ký!");
        }
        String hashPassword = passwordEncoder.encode(request.getPassword());
        User savedUser = RegisterRequestMapper.toEntity(request, hashPassword);
        userRepository.save(savedUser);
    }

    @Transactional
    @Override
    public UserResponse login(LoginRequest request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getPassword()));
        } catch (DisabledException e) {
            User user = findUser(request.getLogin())
                    .orElseThrow(() -> new DisabledException("Tên đăng nhập hoặc mật khẩu không đúng.", e));
            if (user.getStatus() == Status.PENDING) {
                throw new DisabledException("Tài khoản đang chờ duyệt", e);
            }
            throw new DisabledException("Tài khoản đã bị khóa", e);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        User user = findUser(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tên đăng nhập hoặc mật khẩu không đúng."));
        return userResponseMapper.fromEntity(user);
    }

    private Optional<User> findUser(String keyword) {
        return userRepository.findUserByUsernameOrEmail(keyword, keyword);
    }

    private boolean existsByKeyword(String keyword) {
        return userRepository.existsByUsernameOrEmail(keyword, keyword);
    }

    @Override
    public void forgotPassword(String email) {
        User user = findUser(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));

        SendOtpRequest request = SendOtpRequest.builder()
                .destination(user.getEmail())
                .purpose(OtpPurpose.FORGOT_PASSWORD)
                .build();

        otpService.send(request);
    }

    @Override
    public boolean verifyOtp(String destination, String code) {
        try {
            VerifyOtpRequest request = VerifyOtpRequest.builder()
                    .destination(destination)
                    .code(code)
                    .purpose(OtpPurpose.FORGOT_PASSWORD)
                    .build();

            otpService.verify(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
