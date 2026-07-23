package com.attendenceSystem.module.report.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.report.entity.Report;
import com.attendenceSystem.module.report.entity.ReportShare;
import com.attendenceSystem.module.user.entity.User;

@Repository
public interface ReportShareRepository extends JpaRepository<ReportShare, Long> {

    @Query("SELECT rs.report FROM ReportShare rs WHERE rs.user = :user ORDER BY rs.report.createdAt DESC")
    Page<Report> findReportsSharedWithUser(User user, Pageable pageable);
}
