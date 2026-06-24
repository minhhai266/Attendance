package com.attendenceSystem.module.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.log.entity.enums.LogAction;
import com.attendenceSystem.module.log.entity.enums.LogEntityType;
import com.attendenceSystem.module.user.dto.request.LoginRequest;
import com.attendenceSystem.module.user.dto.request.RegisterRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(Routes.Auth.ROOT)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping(Routes.Auth.LOGIN)
    public String toLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return Views.Auth.LOGIN;
    }

    @GetMapping(Routes.Auth.REGISTER)
    public String toRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return Views.Auth.REGISTER;
    }

    @GetMapping(Routes.Auth.FORGOT_PASSWORD)
    public String toForgotPasswordPage() {
        return Views.Auth.FORGOT_PASSWORD;
    }

    @GetMapping(Routes.Auth.VERIFY_OTP)
    public String toVerifyOtpPage() {
        return Views.Auth.VERIFY_OTP;
    }

    @GetMapping(Routes.Auth.CHANGE_PASSWORD)
    public String toChangePasswordPage() {
        return Views.Auth.CHANGE_PASSWORD;
    }

    @PostMapping(Routes.Auth.LOGIN)
    public String login(
            @Valid @ModelAttribute LoginRequest request,
            BindingResult result,
            Model model,
            HttpServletRequest req) {
        if (result.hasErrors()) {
            return Views.Auth.LOGIN;
        }
        try {
            UserResponse user = authService.login(request);
            req.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
            return switch (user.role()) {
                case "ADMIN" -> Routes.REDIRECT + Routes.Dashboard.ROOT + Routes.Dashboard.ADMIN;
                case "MANAGER" -> Routes.REDIRECT + Routes.Dashboard.ROOT + Routes.Dashboard.MANAGER;
                case "STUDENT" -> Routes.REDIRECT + Routes.Dashboard.ROOT + Routes.Dashboard.STUDENT;
                default -> Routes.REDIRECT + Routes.Dashboard.ROOT;
            };
        } catch (AuthenticationException e) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng.");
            return Views.Auth.LOGIN;
        }
    }

    @PostMapping(Routes.Auth.REGISTER)
    public String register(
            @Valid @ModelAttribute RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return Views.Auth.REGISTER;
        }
        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng kí thành công");
            return Routes.REDIRECT + Routes.Auth.ROOT + Routes.Auth.LOGIN;
        } catch (IllegalArgumentException e) {
            request.setPassword("");
            request.setRePassword("");
            model.addAttribute("errorMessage", "Đăng kí tài khoản thất bại");
            return Views.Auth.REGISTER;
        }
    }

    @PostMapping(Routes.Auth.LOGOUT)
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler()
                    .logout(request, response, authentication);
        }

        return Routes.REDIRECT+"/";
    }
}
