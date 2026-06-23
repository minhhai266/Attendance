package com.attendenceSystem.module.attendance.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.mapper.response.LeaveRequestResponseMapper;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.repository.UserRepository;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final AttendanceResponseMapper attendanceResponseMapper;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestResponseMapper leaveRequestResponseMapper;

    @Transactional
    @Override
    public AttendanceResponse checkIn() {
        User user = getCurrentUser();

        LocalDate today = LocalDate.now();
        Optional<AttendanceRecord> existing = attendanceRecordRepository
                .findByUserAndAttendanceDateWithLock(user, today);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Bạn đã điểm danh hôm nay");
        }
        AttendanceRecord attendanceRecord = AttendanceRecord.builder()
                .user(user)
                .attendanceDate(today)
                .checkInTime(Instant.now())
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRecordRepository.save(attendanceRecord);
        return attendanceResponseMapper.fromEntity(attendanceRecord);
    }

    @Transactional
    @Override
    public AttendanceResponse checkOut() {
        User user = getCurrentUser();

        LocalDate today = LocalDate.now();
        AttendanceRecord attendance = attendanceRecordRepository
                .findByUserAndAttendanceDate(user, today)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bạn không điểm danh hôm nay nên không thể checkout"));
        if (attendance.getCheckOutTime() != null) {
            throw new IllegalArgumentException("Bạn đã checkout rồi");
        }
        if (attendance.getStatus() != AttendanceStatus.PRESENT) {
            throw new IllegalArgumentException("Trạng thái điểm danh không hợp lệ");
        }
        attendance.setCheckOutTime(Instant.now());
        attendanceRecordRepository.save(attendance);
        return attendanceResponseMapper.fromEntity(attendance);
    }

    @Override
    public Page<AttendanceResponse> getAttendanceHistory(Pageable pageable) {
        User user = getCurrentUser();

        return attendanceRecordRepository
                .findByUser(user, pageable)
                .map(attendanceResponseMapper::fromEntity);
    }

    @Transactional
    @Override
    public LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        User user = getCurrentUser();
        LeaveRequest leave = LeaveRequest.builder()
                .user(user)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(AttendanceStatus.LEAVE)
                .build();
        LeaveRequest saved = leaveRequestRepository.save(leave);
        return leaveRequestResponseMapper.fromEntity(saved);
    }

    @Override
    public Page<LeaveRequestResponse> getLeaveRequests(Pageable pageable) {
        User user = getCurrentUser();
        return leaveRequestRepository.findByUser(user, pageable)
                .map(leaveRequestResponseMapper::fromEntity);
    }

    private User getCurrentUser() {
        String currentUser = SecurityUtil.getCurrentUserName();
        if (currentUser == null || currentUser.isEmpty() || currentUser.isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return userRepository
                .findByUsername(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }
}
