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
import com.attendenceSystem.module.user.dto.response.UserSimpleResponse;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.provider.UserContextProvider;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping(Routes.API + Routes.Report.ROOT)
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;
    private final UserContextProvider userContextProvider;

    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getAllReports(Pageable pageable) {
        Page<ReportResponse> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping(Routes.Report.MY_REPORT)
    public ResponseEntity<Page<ReportResponse>> getMyReports(Pageable pageable) {
        Page<ReportResponse> reports = reportService.getMyReports(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping(Routes.Report.SHARED)
    public ResponseEntity<Page<ReportResponse>> getSharedWithMe(Pageable pageable) {
        Page<ReportResponse> reports = reportService.getSharedWithMe(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDetailResponse> getReportById(@PathVariable Long id) {
        ReportDetailResponse report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @GetMapping(Routes.Report.DEPARTMENT_USERS)
    public ResponseEntity<List<UserSimpleResponse>> getUsersByDepartment(@PathVariable String departmentId) {
        List<UserSimpleResponse> users = userContextProvider.getFullNamesByDepartmentAndRoleNotAdmin(departmentId);
        return ResponseEntity.ok(users);
    }
}
