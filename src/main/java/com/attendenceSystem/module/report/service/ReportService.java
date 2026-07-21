package com.attendenceSystem.module.report.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.attendenceSystem.module.report.dto.request.CreateReportRequest;
import com.attendenceSystem.module.report.dto.response.ReportDetailResponse;
import com.attendenceSystem.module.report.dto.response.ReportResponse;

public interface ReportService {

    ReportResponse createReport(CreateReportRequest request);

    Page<ReportResponse> getAllReports(Pageable pageable);

    Page<ReportResponse> getMyReports(Pageable pageable);

    ReportDetailResponse getReportById(Long id);
}