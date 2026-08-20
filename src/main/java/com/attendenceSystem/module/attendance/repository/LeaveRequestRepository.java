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
import com.attendenceSystem.module.attendance.repository.projection.EmployeeLeaveList;
import com.attendenceSystem.module.attendance.repository.projection.ManagerLeaveList;
import com.attendenceSystem.module.user.entity.User;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    Page<EmployeeLeaveList> findByUser(User user, Pageable pageable);

    Optional<LeaveRequest> findById(long id);

    @Query("SELECT l.user.id FROM LeaveRequest l WHERE l.status = :status " +
            "AND l.startDate <= :date AND l.endDate >= :date")
    List<Long> findUserIdsOnLeaveForDate(
            @Param("date") LocalDate date,
            @Param("status") LeaveStatus status);

    List<LeaveRequest> findByStatusAndStartDateLessThanEqual(LeaveStatus status, LocalDate date);

    @Query("""
        SELECT l.id AS id, 
               l.reason AS reason, 
               l.status AS status, 
               l.startDate AS startDate, 
               l.endDate AS endDate
        FROM LeaveRequest l 
        WHERE l.user = :user 
          AND (:status IS NULL OR l.status = :status) 
          AND (CAST(:weekStart AS date) IS NULL OR (l.startDate <= :weekEnd AND l.endDate >= :weekStart))
          """)
    Page<EmployeeLeaveList> findLeaveRequestsWithFilters(
        @Param("user") User user,
        @Param("status") LeaveStatus status,
        @Param("weekStart") LocalDate weekStart,
        @Param("weekEnd") LocalDate weekEnd,
        Pageable pageable
    );

    @Query("""
        SELECT l.id AS id, 
               u.fullName AS userFullName, 
               l.startDate AS startDate, 
               l.endDate AS endDate, 
               l.status AS status
        FROM LeaveRequest l 
        JOIN l.user u
        WHERE (:keyword IS NULL OR LOWER(u.fullName) LIKE :keyword)
          AND (:status IS NULL OR l.status = :status)
          AND (CAST(:weekStart AS date) IS NULL OR (l.startDate <= :weekEnd AND l.endDate >= :weekStart))
    """)
    Page<ManagerLeaveList> findAllManagerLeaveRequestsWithFilters(
        @Param("keyword") String keyword,
        @Param("status") LeaveStatus status,
        @Param("weekStart") LocalDate weekStart,
        @Param("weekEnd") LocalDate weekEnd,
        Pageable pageable
    );
}
