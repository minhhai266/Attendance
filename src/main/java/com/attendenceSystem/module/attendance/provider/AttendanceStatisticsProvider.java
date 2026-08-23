package com.attendenceSystem.module.attendance.provider;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.EmployeeAttendanceListResponse;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceCheckStatus;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.mapper.response.EmployeeAttendanceListResponseMapper;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceStatisticsProvider {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceResponseMapper attendanceResponseMapper;
    private final EmployeeAttendanceListResponseMapper employeeAttendanceListResponseMapper;

    public long getCountByDateAndStatus(LocalDate date, AttendanceStatus status) {
        return attendanceRecordRepository.countByAttendanceDateAndStatus(date, status);
    }

    public long getCountByDateAndCheckStatus(LocalDate date, AttendanceCheckStatus status) {
        return attendanceRecordRepository.countByAttendanceDateAndCheckStatus(date, status.getValue());
    }

    public Page<AttendanceResponse> getRecentHistory(int size) {
        return attendanceRecordRepository
                .findAllByOrderByAttendanceDateDesc(PageRequest.of(0, size))
                .map(attendanceResponseMapper::fromEntity);
    }

    public long getTotalDaysByUser(User user) {
        return attendanceRecordRepository.countByUser(user);
    }

    public long getAttendedDaysByUser(User user) {
        return attendanceRecordRepository.countByUserAndCheckInTimeNotNullAndCheckOutTimeNotNull(user);
    }

    public Page<EmployeeAttendanceListResponse> getRecentHistoryByUser(User user, int size) {
        return attendanceRecordRepository
                .findByUser(user, PageRequest.of(0, size))
                .map(employeeAttendanceListResponseMapper::fromEntity);
    }
}
