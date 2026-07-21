package com.attendenceSystem.module.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.request.UpdateUserInformationRequest;
import com.attendenceSystem.module.user.dto.response.UserInformationResponse;
import com.attendenceSystem.module.user.service.UserService;
import com.attendenceSystem.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping(Routes.User.ROOT)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping(Routes.User.PROFILE)
    public String toProfilePage(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        model.addAttribute("updateUserInformationRequest", new UpdateUserInformationRequest());
        return Views.User.PROFILE;
    }

    @PostMapping(Routes.User.CHANGE_PASSWORD)
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
            }
            ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, new UpdatePasswordRequest(newPassword, confirmPassword));
            userService.changePassword(request);
            return Routes.REDIRECT + Routes.User.ROOT + Routes.User.PROFILE + "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return Views.User.PROFILE;
        }
    }

    @PostMapping(Routes.User.UPDATE_PASSWORD)
    public String updatePasswordWithOtp(
            @RequestParam("destination") String destination,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpServletRequest request,
            Model model) {
        try {
            // Kiểm tra OTP đã được verify chưa (thông qua session)
            HttpSession session = request.getSession(false);
            Boolean otpVerified = (session != null) ? (Boolean) session.getAttribute("otpVerified") : null;
            String otpEmail = (session != null) ? (String) session.getAttribute("otpEmail") : null;
            
            if (otpVerified == null || !otpVerified || otpEmail == null || !otpEmail.equals(destination)) {
                throw new IllegalArgumentException("Vui lòng xác thực OTP trước khi đổi mật khẩu");
            }
            
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
            }
            UpdatePasswordWithOtpRequest updateRequest = new UpdatePasswordWithOtpRequest(destination, new UpdatePasswordRequest(password, confirmPassword));
            userService.updatePasswordWithOtp(updateRequest);
            
            // Clear session attributes after successful password change
            if (session != null) {
                session.removeAttribute("otpVerified");
                session.removeAttribute("otpEmail");
            }
            
            return Routes.REDIRECT + Routes.Auth.ROOT + Routes.Auth.LOGIN+ "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", destination);
            return Views.Auth.CHANGE_PASSWORD;
        }
    }

    @PostMapping(Routes.User.UPDATE_INFORMATION)
    public String updateUserInformation(@Valid @ModelAttribute UpdateUserInformationRequest request, 
                                       BindingResult result,
                                       Model model) {
        try {
            if (result.hasErrors()) {
                return Views.User.PROFILE;
            }
            
            UserInformationResponse updatedUser = userService.updateUserInformation(request);
            
            var auth = SecurityContextHolder.getContext().getAuthentication();
            var newDetails = CustomUserDetails.fromUserInformationResponse(updatedUser);
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                    newDetails, auth.getCredentials(), auth.getAuthorities()
                )
            );
            
            return Routes.REDIRECT + Routes.User.ROOT + Routes.User.PROFILE + "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return Views.User.PROFILE;
        }
    }

}