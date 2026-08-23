package com.attendenceSystem.module.attendance.mapper.response;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.ManagerAttendanceListResponse;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.repository.projection.ManagerAttendanceList;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ManagerAttendanceListResponseMapper {
    private final AttendanceCalculator attendanceCalculator;

    public ManagerAttendanceListResponse fromEntity(ManagerAttendanceList list) {
        Set<AttendanceCheckStatus> checkStatuses = list.getCheckStatuses();
        boolean late = checkStatuses.contains(AttendanceCheckStatus.LATE);
        boolean earlyLeave = checkStatuses.contains(AttendanceCheckStatus.EARLY_LEAVE);
        return ManagerAttendanceListResponse.builder()
                .id(list.getId())
                .attendanceDate(list.getAttendanceDate())
                .userFullName(list.getUser().getFullName())
                .checkInTime(list.getCheckInTime())
                .checkOutTime(list.getCheckOutTime())
                .status(list.getStatus())
                .checkStatuses(checkStatuses)
                .note(list.getNote())
                .late(late)
                .earlyLeave(earlyLeave)
                .workingMinutes(attendanceCalculator.workingMinutes(list.getCheckInTime(), list.getCheckOutTime()))
                .build();
    }
}