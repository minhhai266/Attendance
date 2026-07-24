package com.attendenceSystem.module.dashboard.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.dashboard.dto.response.AdminDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.EmployeeDashboardResponse;
import com.attendenceSystem.module.dashboard.dto.response.ManagerDashboardResponse;
import com.attendenceSystem.module.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Routes.API + Routes.Dashboard.ROOT)
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping(Routes.Role.ADMIN)
    public ResponseEntity<AdminDashboardResponse> admin() {
        AdminDashboardResponse resp = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(resp);
    }

    @GetMapping(Routes.Role.MANAGER)
    public ResponseEntity<ManagerDashboardResponse> manager(Pageable pageable) {
        ManagerDashboardResponse resp = dashboardService.getManagerDashboard();
        return ResponseEntity.ok(resp);
    }

    @GetMapping(Routes.Role.EMPLOYEE)
    public ResponseEntity<EmployeeDashboardResponse> employee(Pageable pageable) {
        EmployeeDashboardResponse resp = dashboardService.getEmployeeDashboard();
        return ResponseEntity.ok(resp);
    }
}
