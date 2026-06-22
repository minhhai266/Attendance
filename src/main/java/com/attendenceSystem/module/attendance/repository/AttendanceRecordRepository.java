package com.attendenceSystem.module.attendance.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.user.entity.User;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByUserAndAttendanceDate(User user, LocalDate attendanceDate);

    Page<AttendanceRecord> findByUser(User user, Pageable pageable);

    
}
