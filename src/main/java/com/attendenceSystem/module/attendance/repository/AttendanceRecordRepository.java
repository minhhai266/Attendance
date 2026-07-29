package com.attendenceSystem.module.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.user.entity.User;
import jakarta.persistence.LockModeType;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
        Optional<AttendanceRecord> findByUserAndAttendanceDate(User user, LocalDate attendanceDate);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT a FROM AttendanceRecord a WHERE a.user = :user AND a.attendanceDate = :date")
        Optional<AttendanceRecord> findByUserAndAttendanceDateWithLock(
                        @Param("user") User user,
                        @Param("date") LocalDate date);

        Page<AttendanceRecord> findByUser(User user, Pageable pageable);

        List<AttendanceRecord> findAllByUser(User user);

        Page<AttendanceRecord> findByUserAndAttendanceDateBetween(
                        User user,
                        LocalDate startDate,
                        LocalDate endDate,
                        Pageable pageable);

        long countByAttendanceDate(LocalDate attendanceDate);

        Page<AttendanceRecord> findAllByOrderByAttendanceDateDesc(Pageable pageable);

        List<AttendanceRecord> findByAttendanceDate(LocalDate attendanceDate);

        List<AttendanceRecord> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);

        List<AttendanceRecord> findByAttendanceDateBetweenAndStatus(
                        LocalDate startDate,
                        LocalDate endDate,
                        AttendanceStatus status);

        List<AttendanceRecord> findAllByOrderByAttendanceDateDesc();

        long countByAttendanceDateAndStatus(LocalDate attendanceDate, AttendanceStatus status);

        long countByUser(User user);

        long countByUserAndCheckInTimeNotNullAndCheckOutTimeNotNull(User user);

        List<AttendanceRecord> findByUserAndAttendanceDateBetween(User user, LocalDate startDate, LocalDate endDate);

        @Query("SELECT a FROM AttendanceRecord a WHERE a.attendanceDate = :date AND a.checkInTime IS NOT NULL AND a.checkOutTime IS NULL")
        List<AttendanceRecord> findRecordsMissingCheckOut(@Param("date") LocalDate date);

        List<AttendanceRecord> findByUserIdIn(List<Long> userIds);

        List<AttendanceRecord> findByUserIdInAndAttendanceDateBetween(List<Long> userIds, LocalDate startDate,
                        LocalDate endDate);

        @Query("SELECT a FROM AttendanceRecord a WHERE a.user = :user " +
                        "AND (:startDate IS NULL OR a.attendanceDate >= :startDate) " +
                        "AND (:endDate IS NULL OR a.attendanceDate <= :endDate) " +
                        "AND (:status IS NULL OR a.status = :status)")
        Page<AttendanceRecord> findFilteredAttendanceHistory(
                        @Param("user") User user,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("status") AttendanceStatus status,
                        Pageable pageable);

}
