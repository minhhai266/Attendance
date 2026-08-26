package com.attendenceSystem.module.attendance.mapper.response;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.EmployeeAttendanceListResponse;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.repository.projection.EmployeeAttendanceList;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeAttendanceListResponseMapper {
        private final AttendanceCalculator attendanceCalculator;

        public EmployeeAttendanceListResponse fromEntity(EmployeeAttendanceList list) {
                Set<AttendanceCheckStatus> checkStatuses = list.getCheckStatuses();
                boolean late = checkStatuses.contains(AttendanceCheckStatus.LATE);
                boolean earlyLeave = checkStatuses.contains(AttendanceCheckStatus.EARLY_LEAVE);
                long workingMinutes = attendanceCalculator.workingMinutes(
                                list.getCheckInTime(),
                                list.getCheckOutTime());
                return EmployeeAttendanceListResponse.builder()
                                .id(list.getId())
                                .attendanceDate(list.getAttendanceDate())
                                .checkInTime(list.getCheckInTime())
                                .checkOutTime(list.getCheckOutTime())
                                .status(list.getStatus().name())
                                .checkStatuses(checkStatuses)
                                .note(list.getNote())
                                .late(late)
                                .earlyLeave(earlyLeave)
                                .workingMinutes(workingMinutes)
                                .build();
        }

}