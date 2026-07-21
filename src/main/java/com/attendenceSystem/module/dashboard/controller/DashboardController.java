package com.attendenceSystem.module.dashboard.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(Routes.Dashboard.ROOT)
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping(Routes.Dashboard.ADMIN)
    public String toAdminDashboardPage(Model model) {
        model.addAttribute("dashboard", dashboardService.getAdminDashboard());
        return Views.Dashboard.ADMIN;
    }

    @GetMapping(Routes.Dashboard.MANAGER)
    public String toManagerDashboardPage(Model model) {
        model.addAttribute("dashboard", dashboardService.getManagerDashboard());
        return Views.Dashboard.MANAGER;
    }

    @GetMapping(Routes.Dashboard.EMPLOYEE)
    public String toEmployeeDashboardPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== DASHBOARD AUTH ===");
        System.out.println(auth);
        if (auth != null) {
            System.out.println("name = " + auth.getName());
            System.out.println("authorities = " + auth.getAuthorities());
            System.out.println("authenticated = " + auth.isAuthenticated());
        }
        model.addAttribute("dashboard", dashboardService.getEmployeeDashboard());
        return Views.Dashboard.EMPLOYEE;
    }
}
