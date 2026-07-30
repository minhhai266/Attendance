package com.attendenceSystem.module.report.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.report.entity.Report;
import com.attendenceSystem.module.report.entity.enums.ReportStatus;
import com.attendenceSystem.module.user.entity.User;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByEmployee(User employee);
    long countByEmployeeAndStatus(User employee, ReportStatus status);
    Page<Report> findByEmployeeOrderByCreatedAtDesc(User employee, Pageable pageable);
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.shares rs LEFT JOIN FETCH rs.user WHERE r.id = :id")
    Optional<Report> findByIdWithShares(@Param("id") Long id);
}
