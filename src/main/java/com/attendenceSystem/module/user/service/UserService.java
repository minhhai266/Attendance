package com.attendenceSystem.module.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.user.dto.response.UserResponse;

public interface UserService {
    Page<UserResponse> getUsers(Pageable pageable);
    UserResponse getById(Long id);
    void deleteUser(Long id);
    void deactiveUser(Long id);
    void activateUser(Long id);
}
