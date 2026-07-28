package com.attendenceSystem.module.user.service.impl;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.attendenceSystem.exception.custom.BadRequestException;
import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.request.UpdateUserInformationRequest;
import com.attendenceSystem.module.user.dto.response.UserInformationResponse;
import com.attendenceSystem.module.user.entity.User;

import com.attendenceSystem.module.user.mapper.response.UserInformationResponseMapper;
import com.attendenceSystem.module.user.provider.UserContextProvider;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.module.user.service.UserService;
import com.attendenceSystem.security.CustomUserDetails;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserInformationResponseMapper userInformationResponseMapper;
    private final UserContextProvider userContextProvider;

    @Transactional
    @Override
    public void changePassword(final ChangePasswordRequest request) {
        User currentUser = userContextProvider.getCurrentUserEntity();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        if (!request.getRequest().getPassword().equals(request.getRequest().getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
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
    public void updatePasswordWithOtp(
            final UpdatePasswordWithOtpRequest request,
            String otpEmail,
            Boolean isVerified) {
        if (isVerified == null || !isVerified || otpEmail == null || !otpEmail.equals(request.getDestination())) {
            throw new BadRequestException("Vui lòng xác nhận email trước khi đổi mật khẩu");
        }

        User user = userRepository.findByUsernameOrEmail(request.getDestination(), request.getDestination())
                .orElseThrow(() -> new BadRequestException(
                        "Không tìm thấy người dùng với email: " + request.getDestination()));

        updatePassword(user, request.getRequest());

    }

    @Transactional
    @Override
    public UserInformationResponse updateUserInformation(final UpdateUserInformationRequest request) {
        User currentUser = userContextProvider.getCurrentUserEntity();
        if (StringUtils.hasText(request.getFullName())) {
            currentUser.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            if (!request.getPhone().equals(currentUser.getPhone())
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
        if (request.getSpecialization() != null) {
            currentUser.setSpecialization(request.getSpecialization());
        }
        userRepository.save(currentUser);
        UserInformationResponse response = userInformationResponseMapper.fromEntity(currentUser);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            CustomUserDetails userDetails = CustomUserDetails.fromUserInformationResponse(response);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, auth.getCredentials(), auth.getAuthorities()));
        }
        return response;
    }
}
