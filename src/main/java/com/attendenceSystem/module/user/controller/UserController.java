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

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Controller
@RequestMapping(Routes.User.ROOT)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public String toListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("accounts", userService.getAccounts(pageable));
        return Views.User.LIST;
    }

    @GetMapping("/{id}")
    public String toDetailPage(@PathVariable("id") Long id, Model model) {
        UserResponse account = userService.getById(id);
        model.addAttribute("account", account);
        return Views.User.DETAIL;
    }

    @GetMapping(Routes.User.PROFILE)
    public String toProfilePage() {
        return Views.User.PROFILE;
    }

}
