package com.attendenceSystem.module.report.service.impl;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.exception.custom.ResourceNotFoundException;
import com.attendenceSystem.module.report.dto.request.CreateReportRequest;
import com.attendenceSystem.module.report.dto.response.ReportDetailResponse;
import com.attendenceSystem.module.report.dto.response.ReportResponse;
import com.attendenceSystem.module.report.entity.Report;
import com.attendenceSystem.module.report.mapper.request.CreateReportRequestMapper;
import com.attendenceSystem.module.report.mapper.response.ReportDetailResponseMapper;
import com.attendenceSystem.module.report.mapper.response.ReportResponseMapper;
import com.attendenceSystem.module.report.repository.ReportRepository;
import com.attendenceSystem.module.report.service.ReportService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ReportResponse createReport(final CreateReportRequest request) {
        User employee = findCurrentUser();

        Report report = CreateReportRequestMapper.toEntity(request, employee);
        report.setCreatedAt(Instant.now());

        Report savedReport = reportRepository.save(report);
        return ReportResponseMapper.fromEntity(savedReport);
    }

    @Override
    public Page<ReportResponse> getAllReports(final Pageable pageable) {
        return reportRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ReportResponseMapper::fromEntity);
    }

    @Override
    public Page<ReportResponse> getMyReports(final Pageable pageable) {
        User employee = findCurrentUser();
        return reportRepository.findByEmployeeOrderByCreatedAtDesc(employee, pageable)
                .map(ReportResponseMapper::fromEntity);
    }

    @Override
    public ReportDetailResponse getReportById(final Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo cáo với id: " + id));
        return ReportDetailResponseMapper.fromEntity(report);
    }

    private User findCurrentUser() {
        if (!SecurityUtil.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String currentUsername = SecurityUtil.getCurrentUserName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng với tên đăng nhập: " + currentUsername));
    }
}