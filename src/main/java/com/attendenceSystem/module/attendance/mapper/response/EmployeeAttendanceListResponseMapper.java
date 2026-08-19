package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.EmployeeAttendanceListResponse;
import com.attendenceSystem.module.attendance.repository.projection.EmployeeAttendanceList;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeAttendanceListResponseMapper {
    public EmployeeAttendanceListResponse fromEntity(EmployeeAttendanceList list) {
        return EmployeeAttendanceListResponse.builder()
                .attendanceDate(list.getAttendanceDate())
                .checkInTime(list.getCheckInTime())
                .checkOutTime(list.getCheckOutTime())
                .status(list.getStatus())
                .note(list.getNote())
                .build();
    }
}