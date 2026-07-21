package com.attendenceSystem.module.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.user.dto.response.UserResponse;

public interface AccountService {
    Page<UserResponse> getUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    void deactivateUser(Long id);
    void activateUser(Long id);}
