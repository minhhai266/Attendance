package com.attendenceSystem.module.attendance.mapper.response;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceResponseMapper {
    private final AttendanceCalculator attendanceCalculator;

    public AttendanceResponse fromEntity(AttendanceRecord attendance) {
        Set<AttendanceCheckStatus> checkStatuses = attendance.getCheckStatuses() != null
                ? attendance.getCheckStatuses()
                : Set.of();
        boolean late = checkStatuses.contains(AttendanceCheckStatus.LATE);
        boolean earlyLeave = checkStatuses.contains(AttendanceCheckStatus.EARLY_LEAVE);
        long workingMinutes = attendanceCalculator.workingMinutes(
                attendance.getCheckInTime(),
                attendance.getCheckOutTime()
        );

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .fullName(attendance.getUser().getFullName())
                .department(attendance.getUser().getDepartment())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .status(attendance.getStatus())
                .checkStatuses(checkStatuses)
                .note(attendance.getNote())
                .late(late)
                .earlyLeave(earlyLeave)
                .workingMinutes(workingMinutes)
                .build();
    }
}