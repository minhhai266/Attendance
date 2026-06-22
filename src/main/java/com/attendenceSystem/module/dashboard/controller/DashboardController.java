package com.attendenceSystem.module.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(Routes.Dashboard.ROOT)
public class DashboardController {
    // private final DashboardService dashboardService;

    // @GetMapping(Routes.Dashboard.ADMIN)
    // public String toAdminDashboardPage(Model model) {
    //     model.addAttribute("dashboard", dashboardService.getAdminDashboard());
    //     return Views.Dashboard.ADMIN;
    // }

    // @GetMapping(Routes.Dashboard.MANAGER)
    // public String toManagerDashboardPage(Model model) {
    //     model.addAttribute("dashboard", dashboardService.getManagerDashboard());
    //     return Views.Dashboard.MANAGER;
    // }

    // @GetMapping(Routes.Dashboard.EMPLOYEE)
    // public String toEmployeeDashboardPage(Model model) {
    //     model.addAttribute("dashboard", dashboardService.getEmployeeDashboard());
    //     return Views.Dashboard.EMPLOYEE;
    // }

}
