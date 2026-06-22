package com.attendenceSystem.module.report.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.report.entity.Report;
import com.attendenceSystem.module.report.entity.enums.ReportStatus;
import com.attendenceSystem.module.user.entity.User;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByEmployee(User employee);
    long countByEmployeeAndStatus(User employee, ReportStatus status);
    Page<Report> findByEmployeeOrderByCreatedAtDesc(User employee, Pageable pageable);
}
