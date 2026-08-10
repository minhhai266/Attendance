package com.attendenceSystem.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.security.CustomUserDetails;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityUtil {

    public CustomUserDetails getCurrentUser() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }

        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public Role getCurrentUserRole() {
        return Role.valueOf(getCurrentUser().getRole());
    }

    public String getCurrentUserName() {
        return getCurrentUser().getUsername();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
