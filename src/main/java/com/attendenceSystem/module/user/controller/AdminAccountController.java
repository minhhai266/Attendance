package com.attendenceSystem.module.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.AccountService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(Routes.Role.ADMIN)
public class AdminAccountController {
    private final AccountService accountService;

    @GetMapping(Routes.Account.ROOT)
    public String toListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("users", accountService.getUsers(pageable));
        model.addAttribute("currentUsername", SecurityUtil.getCurrentUserName());
        model.addAttribute("currentRole", SecurityUtil.getCurrentUserRole());
        return Views.User.LIST;
    }

    @GetMapping(Routes.Account.ROOT + "/{id}")
    public String toDetailPage(@PathVariable("id") Long id, Model model) {
        UserResponse user = accountService.getUserById(id);
        model.addAttribute("user", user);
        return Views.User.DETAIL;
    }

    @PostMapping(Routes.Account.ROOT + Routes.Action.DEACTIVATE + "/{id}")
    public String deactiveUser(@PathVariable("id") Long id) {
        accountService.deactivateUser(id);
        return Routes.REDIRECT + Routes.Role.ADMIN + Routes.Account.ROOT;
    }

    @PostMapping(Routes.Account.ROOT + Routes.Action.ACTIVATE + "/{id}")
    public String activateUser(@PathVariable("id") Long id) {
        accountService.activateUser(id);
        return Routes.REDIRECT + Routes.Role.ADMIN + Routes.Account.ROOT;
    }
}
