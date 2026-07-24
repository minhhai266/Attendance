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
import org.springframework.web.bind.annotation.RequestParam;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.service.AccountService;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@RequestMapping(Routes.Role.MANAGER)
public class ManagerAccountController {
    private final AccountService accountService;

    @GetMapping(Routes.Account.ROOT)
    public String toEmployeeListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("users", accountService.getEmployees(pageable));
        model.addAttribute("currentUsername", SecurityUtil.getCurrentUserName());
        model.addAttribute("currentRole", SecurityUtil.getCurrentUserRole());
        model.addAttribute("departments", Department.values());
        return Views.Account.EMPLOYEE_LIST;
    }

    @GetMapping(Routes.Account.ROOT + "/{id}")
    public String toEmployeeDetailPage(@PathVariable("id") Long id, Model model) {
        UserResponse user = accountService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("departments", Department.values());
        return Views.Account.EMPLOYEE_DETAIL;
    }

    @PostMapping(Routes.Account.ROOT + Routes.Action.UPDATE + "/{id}")
    public String changeEmployeeDepartment(@PathVariable("id") Long id,
            @RequestParam("department") String department) {
        accountService.changeDepartment(id, department);
        return Routes.REDIRECT + Routes.Role.MANAGER + Routes.Account.ROOT;
    }
}
