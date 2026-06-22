package com.attendenceSystem.module.attendance.mapper.response;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceResponseMapper {
    public AttendanceResponse fromEntity(AttendanceRecord attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .fullName(attendance.getUser().getFullName())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .status(attendance.getStatus())
                .note(attendance.getNote())
                .build();
    }
}
