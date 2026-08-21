package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.EmployeeAttendanceListResponse;
import com.attendenceSystem.module.attendance.repository.projection.EmployeeAttendanceList;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeAttendanceListResponseMapper {
    private final AttendanceCalculator attendanceCalculator;

    public EmployeeAttendanceListResponse fromEntity(EmployeeAttendanceList list) {
        boolean late = attendanceCalculator.isLate(list.getCheckInTime())
                || attendanceCalculator.isPastAllowedCheckInTime(list.getCheckInTime());
        boolean earlyLeave = attendanceCalculator.isEarlyLeave(list.getCheckOutTime())
                || attendanceCalculator.isBeforeMinCheckOutTime(list.getCheckOutTime());
        long workingMinutes = attendanceCalculator.workingMinutes(
                list.getCheckInTime(),
                list.getCheckOutTime());
        return EmployeeAttendanceListResponse.builder()
                .id(list.getId())
                .attendanceDate(list.getAttendanceDate())
                .checkInTime(list.getCheckInTime())
                .checkOutTime(list.getCheckOutTime())
                .status(list.getStatus())
                .note(list.getNote())
                .late(late)
                .earlyLeave(earlyLeave)
                .workingMinutes(workingMinutes)
                .build();
    }

}