package com.attendenceSystem.module.report.provider;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.report.repository.ReportRepository;
import com.attendenceSystem.module.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportStatisticsProvider {
    private final ReportRepository reportRepository;

    public long countReportsByEmployee(User user){
        return reportRepository.countByEmployee(user);
    }
}
