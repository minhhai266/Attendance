package com.attendenceSystem.module.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.AccountService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping(Routes.Account.ADMIN)
    public String toListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("users", accountService.getUsers(pageable));
        model.addAttribute("currentUsername", SecurityUtil.getCurrentUserName());
        model.addAttribute("currentRole", SecurityUtil.getCurrentUserRole());
        return Views.User.LIST;
    }

    @GetMapping(Routes.Account.MANAGER)
    public String toEmployeeListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("users", accountService.getEmployees(pageable));
        model.addAttribute("currentUsername", SecurityUtil.getCurrentUserName());
        model.addAttribute("currentRole", SecurityUtil.getCurrentUserRole());
        model.addAttribute("departments", com.attendenceSystem.module.user.entity.enums.Department.values());
        return Views.Account.EMPLOYEE_LIST;
    }

    @GetMapping(Routes.Account.ADMIN + "/{id}")
    public String toDetailPage(@PathVariable("id") Long id, Model model) {
        UserResponse user = accountService.getUserById(id);
        model.addAttribute("user", user);
        return Views.User.DETAIL;
    }

    @GetMapping(Routes.Account.MANAGER + "/{id}")
    public String toEmployeeDetailPage(@PathVariable("id") Long id, Model model) {
        UserResponse user = accountService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("departments", com.attendenceSystem.module.user.entity.enums.Department.values());
        return Views.Account.EMPLOYEE_DETAIL;
    }

    @PostMapping(Routes.Account.MANAGER + Routes.Action.UPDATE + "/{id}")
    public String changeEmployeeDepartment(@PathVariable("id") Long id,
                                           @org.springframework.web.bind.annotation.RequestParam("department") String department) {
        accountService.changeDepartment(id, department);
        return Routes.REDIRECT + Routes.Account.MANAGER;
    }

    @PostMapping(Routes.Account.ADMIN + Routes.Action.DEACTIVATE + "/{id}")
    public String deactiveUser(@PathVariable("id") Long id) {
        accountService.deactivateUser(id);
        return Routes.REDIRECT + Routes.Account.ADMIN;
    }

    @PostMapping(Routes.Account.ADMIN + Routes.Action.ACTIVATE + "/{id}")
    public String activateUser(@PathVariable("id") Long id) {
        accountService.activateUser(id);
        return Routes.REDIRECT + Routes.Account.ADMIN;
    }
}
