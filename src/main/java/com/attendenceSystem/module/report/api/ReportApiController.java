package com.attendenceSystem.module.report.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.report.dto.response.ReportDetailResponse;
import com.attendenceSystem.module.report.dto.response.ReportResponse;
import com.attendenceSystem.module.report.service.ReportService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Routes.API + Routes.Report.ROOT)
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getAllReports(Pageable pageable) {
        Page<ReportResponse> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<Page<ReportResponse>> getMyReports(Pageable pageable) {
        Page<ReportResponse> reports = reportService.getMyReports(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<Page<ReportResponse>> getSharedWithMe(Pageable pageable) {
        Page<ReportResponse> reports = reportService.getSharedWithMe(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDetailResponse> getReportById(@PathVariable Long id) {
        ReportDetailResponse report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/users/by-department/{departmentId}")
    public ResponseEntity<List<User>> getUsersByDepartment(@PathVariable String departmentId) {
        Department department = Department.fromValue(departmentId);
        if (department == null) {
            return ResponseEntity.ok(List.of());
        }
        List<User> users = userRepository.findByDepartmentAndRoleNot(department, Role.ADMIN);
        return ResponseEntity.ok(users);
    }
}
