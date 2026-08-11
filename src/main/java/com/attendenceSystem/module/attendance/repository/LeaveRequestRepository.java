package com.attendenceSystem.module.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.user.entity.User;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    Page<LeaveRequest> findByUser(User user, Pageable pageable);

    Optional<LeaveRequest> findById(long id);

    @Query("SELECT l.user.id FROM LeaveRequest l WHERE l.status = :status " +
            "AND l.startDate <= :date AND l.endDate >= :date")
    List<Long> findUserIdsOnLeaveForDate(
            @Param("date") LocalDate date,
            @Param("status") LeaveStatus status);

    List<LeaveRequest> findByStatusAndStartDateLessThanEqual(LeaveStatus status, LocalDate date);
}
