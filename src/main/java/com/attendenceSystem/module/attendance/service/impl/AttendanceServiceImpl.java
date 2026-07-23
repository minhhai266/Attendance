package com.attendenceSystem.module.attendance.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedInException;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedOutException;
import com.attendenceSystem.module.attendance.exception.InvalidAttendanceStateException;
import com.attendenceSystem.module.attendance.exception.NotCheckedInException;
import com.attendenceSystem.module.attendance.mapper.request.CreateLeaveRequestMapper;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.mapper.response.LeaveDetailResponseMapper;
import com.attendenceSystem.module.attendance.mapper.response.LeaveRequestResponseMapper;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.attendance.util.TimeZoneProvider;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Role;
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
    private final LeaveDetailResponseMapper leaveDetailResponseMapper;
    private final AttendanceCalculator attendanceCalculator;
    private final TimeZoneProvider timeZoneProvider;

    @Value("${attendance.start-work:08:00}")
    private String startWork;

    @Value("${attendance.end-work:17:00}")
    private String endWork;

    @Transactional
    @Override
    public AttendanceResponse checkIn() {
        User user = getCurrentUser();

        LocalDate today = LocalDate.now(timeZoneProvider.getZoneId());
        try {
            Optional<AttendanceRecord> existing = attendanceRecordRepository
                    .findByUserAndAttendanceDateWithLock(user, today);
            if (existing.isPresent()) {
                throw new AlreadyCheckedInException("Bạn đã điểm danh hôm nay");
            }
            Instant checkInTime = Instant.now();
            boolean late = attendanceCalculator.isLate(checkInTime);
            AttendanceStatus status = late ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
            AttendanceRecord attendanceRecord = AttendanceRecord.builder()
                    .user(user)
                    .attendanceDate(today)
                    .checkInTime(checkInTime)
                    .status(status)
                    .note(late ? "Đi muộn" : null)
                    .build();
            attendanceRecordRepository.save(attendanceRecord);
            return attendanceResponseMapper.fromEntity(attendanceRecord);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new AlreadyCheckedInException("Bạn đã điểm danh hôm nay");
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new AlreadyCheckedInException("Dữ liệu đã bị thay đổi, vui lòng thử lại");
        }
    }

    @Transactional
    @Override
    public AttendanceResponse checkOut() {
        User user = getCurrentUser();

        LocalDate today = LocalDate.now(timeZoneProvider.getZoneId());
        AttendanceRecord attendance = attendanceRecordRepository
                .findByUserAndAttendanceDateWithLock(user, today)
                .orElseThrow(() -> new NotCheckedInException(
                        "Bạn không điểm danh hôm nay nên không thể checkout"));
        if (attendance.getCheckOutTime() != null) {
            throw new AlreadyCheckedOutException("Bạn đã checkout rồi");
        }
        if (attendance.getStatus() != AttendanceStatus.PRESENT && attendance.getStatus() != AttendanceStatus.LATE) {
            throw new InvalidAttendanceStateException("Trạng thái điểm danh không hợp lệ");
        }
        Instant checkOutTime = Instant.now();
        if (checkOutTime.isBefore(attendance.getCheckInTime())) {
            throw new InvalidAttendanceStateException("Thời gian checkout phải sau thời gian check-in");
        }
        boolean earlyLeave = attendanceCalculator.isEarlyLeave(checkOutTime);
        attendance.setCheckOutTime(checkOutTime);
        if (earlyLeave) {
            attendance.setNote((attendance.getNote() != null ? attendance.getNote() + "; " : "") + "Về sớm");
        }
        attendanceRecordRepository.save(attendance);
        return attendanceResponseMapper.fromEntity(attendance);
    }

    @Override
    public org.springframework.data.domain.Page<AttendanceResponse> getAttendanceHistory(final org.springframework.data.domain.Pageable pageable) {
        User user = getCurrentUser();

        return attendanceRecordRepository
                .findByUser(user, pageable)
                .map(attendanceResponseMapper::fromEntity);
    }

    @Override
    public ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate) {
        return getManagerStats(departmentId, startDate, endDate, null);
    }

    @Override
    public List<AttendanceResponse> getManagerAttendanceList(String departmentId, LocalDate startDate, LocalDate endDate) {
        return getManagerAttendanceList(departmentId, startDate, endDate, null);
    }

    private List<AttendanceRecord> getFilteredRecords(LocalDate startDate, LocalDate endDate, AttendanceStatus status) {
        if (status != null) {
            return attendanceRecordRepository.findByAttendanceDateBetweenAndStatus(startDate, endDate, status);
        }
        return attendanceRecordRepository.findByAttendanceDateBetween(startDate, endDate);
    }

    public ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate, AttendanceStatus status) {
        List<User> employees;
        if (departmentId != null && !departmentId.isEmpty()) {
            Department dept = Department.fromValue(departmentId);
            if (dept == null) {
                employees = userRepository.findByRoleNot(Role.ADMIN);
            } else {
                employees = userRepository.findByDepartmentAndRoleNot(dept, Role.ADMIN);
            }
        } else {
            employees = userRepository.findByRoleNot(Role.ADMIN);
        }

        long totalEmployees = employees.size();
        if (totalEmployees == 0) {
            return ManagerStatsResponse.builder()
                    .totalEmployees(0)
                    .checkedIn(0)
                    .checkedOut(0)
                    .lateArrivals(0)
                    .absent(0)
                    .build();
        }

        LocalDate effectiveStartDate = startDate;
        LocalDate effectiveEndDate = endDate;
        if (effectiveStartDate == null && effectiveEndDate == null) {
            effectiveStartDate = LocalDate.now(timeZoneProvider.getZoneId()).minusMonths(6);
            effectiveEndDate = LocalDate.now(timeZoneProvider.getZoneId());
        } else if (effectiveStartDate != null && effectiveEndDate == null) {
            effectiveEndDate = effectiveStartDate;
        } else if (effectiveStartDate == null && effectiveEndDate != null) {
            effectiveStartDate = effectiveEndDate;
        }

        List<AttendanceRecord> records = getFilteredRecords(effectiveStartDate, effectiveEndDate, status);

        long checkedIn = 0;
        long checkedOut = 0;
        long lateArrivals = 0;
        long absent = 0;

        for (User emp : employees) {
            boolean hasRecord = false;
            for (AttendanceRecord record : records) {
                if (record.getUser() != null && record.getUser().getId().equals(emp.getId())) {
                    hasRecord = true;
                    if (record.getStatus() == AttendanceStatus.LATE) {
                        lateArrivals++;
                    }
                    checkedIn++;
                    if (record.getCheckOutTime() != null) {
                        checkedOut++;
                    }
                    break;
                }
            }
            if (!hasRecord) {
                absent++;
            }
        }

        return ManagerStatsResponse.builder()
                .totalEmployees(totalEmployees)
                .checkedIn(checkedIn)
                .checkedOut(checkedOut)
                .lateArrivals(lateArrivals)
                .absent(absent)
                .build();
    }

    public List<AttendanceResponse> getManagerAttendanceList(String departmentId, LocalDate startDate, LocalDate endDate, AttendanceStatus status) {
        List<User> employees;
        if (departmentId != null && !departmentId.isEmpty()) {
            Department dept = Department.fromValue(departmentId);
            if (dept == null) {
                employees = userRepository.findByRoleNot(Role.ADMIN);
            } else {
                employees = userRepository.findByDepartmentAndRoleNot(dept, Role.ADMIN);
            }
        } else {
            employees = userRepository.findByRoleNot(Role.ADMIN);
        }

        LocalDate effectiveStartDate = startDate;
        LocalDate effectiveEndDate = endDate;
        if (effectiveStartDate == null && effectiveEndDate == null) {
            effectiveStartDate = LocalDate.now(timeZoneProvider.getZoneId()).minusMonths(6);
            effectiveEndDate = LocalDate.now(timeZoneProvider.getZoneId());
        } else if (effectiveStartDate != null && effectiveEndDate == null) {
            effectiveEndDate = effectiveStartDate;
        } else if (effectiveStartDate == null && effectiveEndDate != null) {
            effectiveStartDate = effectiveEndDate;
        }

        List<AttendanceRecord> records = getFilteredRecords(effectiveStartDate, effectiveEndDate, status);

        return records.stream()
                .map(attendanceResponseMapper::fromEntity)
                .sorted((a, b) -> {
                    if (a.attendanceDate() == null && b.attendanceDate() == null) return 0;
                    if (a.attendanceDate() == null) return 1;
                    if (b.attendanceDate() == null) return -1;
                    return b.attendanceDate().compareTo(a.attendanceDate());
                })
                .toList();
    }

    @Transactional
    @Override
    public LeaveRequestResponse createLeaveRequest(final CreateLeaveRequest request) {
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
        LeaveRequest leave = CreateLeaveRequestMapper.toEntity(request, user);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        return leaveRequestResponseMapper.fromEntity(saved);
    }

    @Override
    public org.springframework.data.domain.Page<LeaveRequestResponse> getLeaveRequests(final org.springframework.data.domain.Pageable pageable) {
        User user = getCurrentUser();
        return leaveRequestRepository.findByUser(user, pageable)
                .map(leaveRequestResponseMapper::fromEntity);
    }

    @Override
    public org.springframework.data.domain.Page<LeaveRequestResponse> getAllLeaveRequests(final org.springframework.data.domain.Pageable pageable) {
        return leaveRequestRepository.findAll(pageable).map(leaveRequestResponseMapper::fromEntity);
    }

    @Override
    public LeaveDetailResponse getLeaveDetail(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID yêu cầu nghỉ phép không hợp lệ");
        }
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu nghỉ phép với ID: " + id));
        return leaveDetailResponseMapper.fromEntity(leave);
    }

    private User getCurrentUser() {
        if (!SecurityUtil.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String currentUsername = SecurityUtil.getCurrentUserName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng với tên đăng nhập: " + currentUsername));
    }
}