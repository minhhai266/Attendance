package com.attendenceSystem.security;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActiveUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!SecurityUtil.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        CustomUserDetails userDetails = SecurityUtil.getCurrentUser();

        Status status = userRepository.findStatusById(userDetails.getId());
        if (Status.ACTIVE.equals(status)) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        boolean isAjaxOrApi = (acceptHeader != null && acceptHeader.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || request.getRequestURI().startsWith("/api/");

        if (isAjaxOrApi) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter()
                    .write("{\"error\": \"ACCOUNT_LOCKED\", \"message\": \"Tài khoản của bạn đã bị khóa.\"}");
        } else {
            response.sendRedirect(request.getContextPath() + Routes.Auth.ROOT + Routes.Auth.LOGIN);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        return uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/webjars/")
                || uri.startsWith("/favicon.ico")
                || uri.startsWith("/error")
                || uri.startsWith("/uploads/")
                || uri.equals("/")
                || uri.equals("/home")
                || uri.equals(Routes.Auth.ROOT + Routes.Auth.LOGIN)
                || uri.equals(Routes.Auth.ROOT + Routes.Auth.LOGOUT);
    }
}