package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.ManagerAttendanceListResponse;
import com.attendenceSystem.module.attendance.repository.projection.ManagerAttendanceList;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ManagerAttendanceListResponseMapper {
    private final AttendanceCalculator attendanceCalculator;

    public ManagerAttendanceListResponse fromEntity(ManagerAttendanceList list){
        return ManagerAttendanceListResponse.builder()
        .attendanceDate(list.getAttendanceDate())
        .userFullName(list.getUserFullName())
        .checkInTime(list.getCheckInTime())
        .checkOutTime(list.getCheckOutTime())
        .status(list.getStatus())
        .note(list.getNote())
        .late(attendanceCalculator.isLate(list.getCheckInTime())
                || attendanceCalculator.isPastAllowedCheckInTime(list.getCheckInTime()))
        .earlyLeave(attendanceCalculator.isEarlyLeave(list.getCheckOutTime())
                || attendanceCalculator.isBeforeMinCheckOutTime(list.getCheckOutTime()))
        .workingMinutes(attendanceCalculator.workingMinutes(list.getCheckInTime(), list.getCheckOutTime()))
        .build();
    }
}