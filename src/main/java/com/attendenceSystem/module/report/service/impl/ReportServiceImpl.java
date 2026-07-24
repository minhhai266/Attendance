package com.attendenceSystem.module.report.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.exception.custom.ResourceNotFoundException;
import com.attendenceSystem.module.report.dto.request.CreateReportRequest;
import com.attendenceSystem.module.report.dto.response.ReportDetailResponse;
import com.attendenceSystem.module.report.dto.response.ReportResponse;
import com.attendenceSystem.module.report.entity.Report;
import com.attendenceSystem.module.report.entity.ReportShare;
import com.attendenceSystem.module.report.mapper.request.CreateReportRequestMapper;
import com.attendenceSystem.module.report.mapper.response.ReportDetailResponseMapper;
import com.attendenceSystem.module.report.mapper.response.ReportResponseMapper;
import com.attendenceSystem.module.report.repository.ReportRepository;
import com.attendenceSystem.module.report.repository.ReportShareRepository;
import com.attendenceSystem.module.report.service.ReportService;
import com.attendenceSystem.module.storage.service.FileStorageService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportShareRepository reportShareRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.storage.upload-dir:uploads}")
    private String storageBaseDir;

    @Transactional
    @Override
    public ReportResponse createReport(final CreateReportRequest request) {
        User employee = findCurrentUser();

        Report report = CreateReportRequestMapper.toEntity(request, employee);

        // Xử lý upload files
        if (request.getFiles() != null && request.getFiles().length > 0) {
            List<String> filePaths = new ArrayList<>();
            for (MultipartFile file : request.getFiles()) {
                if (!file.isEmpty()) {
                    try {
                        String filePath = fileStorageService.saveFile(file, "reports");
                        filePaths.add(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException("Không thể lưu file đính kèm: " + e.getMessage(), e);
                    }
                }
            }
            if (!filePaths.isEmpty()) {
                report.setAttachmentFiles(String.join(",", filePaths));
            }
        }

        Report savedReport = reportRepository.save(report);

        // Save shared users
        if (request.getSharedUserIds() != null && request.getSharedUserIds().length > 0) {
            List<ReportShare> shares = new ArrayList<>();
            for (Long userId : request.getSharedUserIds()) {
                User sharedUser = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy người dùng với id: " + userId));
                shares.add(ReportShare.builder()
                        .report(savedReport)
                        .user(sharedUser)
                        .build());
            }
            reportShareRepository.saveAll(shares);
        }

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
    public Page<ReportResponse> getSharedWithMe(final Pageable pageable) {
        User currentUser = findCurrentUser();
        return reportShareRepository.findReportsSharedWithUser(currentUser, pageable)
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
