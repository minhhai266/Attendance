package com.attendenceSystem.module.attendance.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.user.entity.User;
import jakarta.persistence.LockModeType;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByUserAndAttendanceDate(User user, LocalDate attendanceDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AttendanceRecord a WHERE a.user = :user AND a.attendanceDate = :date")
    Optional<AttendanceRecord> findByUserAndAttendanceDateWithLock(@Param("user") User user, @Param("date") LocalDate date);

    Page<AttendanceRecord> findByUser(User user, Pageable pageable);

    long countByAttendanceDate(LocalDate attendanceDate);

    Page<AttendanceRecord> findAllByOrderByAttendanceDateDesc(Pageable pageable);
}
