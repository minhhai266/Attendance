package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceResponseMapper {
    private final AttendanceCalculator attendanceCalculator;

    public AttendanceResponse fromEntity(AttendanceRecord attendance) {
        boolean late = attendanceCalculator.isLate(attendance.getCheckInTime());
        boolean earlyLeave = attendanceCalculator.isEarlyLeave(attendance.getCheckOutTime());
        long workingMinutes = attendanceCalculator.workingMinutes(attendance.getCheckInTime(), attendance.getCheckOutTime());

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .fullName(attendance.getUser().getFullName())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .status(attendance.getStatus())
                .note(attendance.getNote())
                .late(late)
                .earlyLeave(earlyLeave)
                .workingMinutes(workingMinutes)
                .build();
    }
}
