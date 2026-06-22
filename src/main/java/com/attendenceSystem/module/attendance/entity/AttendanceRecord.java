package com.attendenceSystem.module.attendance.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AttendanceRecord {
    private Long id;

    private User user;

    private Instant checkInTime;
    
    private LocalDate attendanceDate;

    private Instant checkOutTime;

    private AttendanceStatus status;

    private String note;
}
