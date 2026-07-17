package com.attendenceSystem.module.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.UserService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(Routes.User.ROOT)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public String toListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("users", userService.getUsers(pageable));
        model.addAttribute("currentUsername", SecurityUtil.getCurrentUserName());
        model.addAttribute("currentRole", SecurityUtil.getCurrentUserRole());
        return Views.User.LIST;
    }

    @GetMapping("/{id}")
    public String toDetailPage(@PathVariable("id") Long id, Model model) {
        UserResponse user = userService.getById(id);
        model.addAttribute("user", user);
        return Views.User.DETAIL;
    }

    @GetMapping(Routes.User.PROFILE)
    public String toProfilePage() {
        return Views.User.PROFILE;
    }

    @PostMapping(Routes.Action.DEACTIVATE + "/{id}")
    public String deactiveUser(@PathVariable("id") Long id) {
        userService.deactiveUser(id);
        return Routes.REDIRECT + Routes.User.ROOT;
    }

    @PostMapping(Routes.Action.ACTIVATE + "/{id}")
    public String activateUser(@PathVariable("id") Long id) {
        userService.activateUser(id);
        return Routes.REDIRECT + Routes.User.ROOT;
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
            Model model) {
        try {
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Mật khẩu xác nhập không khớp");
            }
            UpdatePasswordWithOtpRequest request = new UpdatePasswordWithOtpRequest(destination, new UpdatePasswordRequest(password, confirmPassword));
            userService.updatePasswordWithOtp(request);
            return Routes.REDIRECT + Routes.Auth.ROOT + Routes.Auth.LOGIN+ "?success=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", destination);
            return Views.Auth.CHANGE_PASSWORD;
        }
    }

}
