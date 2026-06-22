package com.attendenceSystem.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.attendenceSystem.module.user.entity.enums.Role;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityUtil {

    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    public Role getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .map(authority -> Role.valueOf(authority.replace("ROLE_", "")))
                .orElse(null);
    }
}
