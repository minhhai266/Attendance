package com.attendenceSystem.module.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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
import com.attendenceSystem.module.user.service.UserService;

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
            ChangePasswordRequest request = new ChangePasswordRequest(
                    oldPassword, new UpdatePasswordRequest(newPassword, confirmPassword));
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
            UpdatePasswordWithOtpRequest updateRequest = new UpdatePasswordWithOtpRequest(
                    destination, new UpdatePasswordRequest(password, confirmPassword));
            userService.updatePasswordWithOtp(updateRequest);

            return Routes.REDIRECT + Routes.Auth.ROOT + Routes.Auth.LOGIN + "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", destination);
            return Views.Auth.CHANGE_PASSWORD;
        }
    }

    @PostMapping(Routes.User.INFORMATION + Routes.Action.UPDATE)
    public String updateUserInformation(@Valid @ModelAttribute UpdateUserInformationRequest request,
            BindingResult result,
            Model model) {
        try {
            if (result.hasErrors()) {
                return Views.User.PROFILE;
            }

            userService.updateUserInformation(request);

            return Routes.REDIRECT + Routes.User.ROOT + Routes.User.PROFILE + "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return Views.User.PROFILE;
        }
    }

}