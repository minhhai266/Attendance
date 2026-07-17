package com.attendenceSystem.module.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.User;

public interface UserService {
    Page<UserResponse> getUsers(Pageable pageable);
    UserResponse getById(Long id);
    void deleteUser(Long id);
    void deactiveUser(Long id);
    void activateUser(Long id);

    void changePassword(ChangePasswordRequest request);
    void updatePassword(User user, UpdatePasswordRequest request);
    void updatePasswordWithOtp(UpdatePasswordWithOtpRequest request);
    
}
