package com.attendenceSystem.module.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.UserService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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


}
