package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.response.AttendanceHistoryStatsResponse;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.model.DateRange;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.provider.UserContextProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceResponseMapper attendanceResponseMapper;
    private final AttendanceCalculator attendanceCalculator;
    private final ZoneId applicationZoneId;
    private final UserContextProvider userContextProvider;

    @Override
    public Page<AttendanceResponse> getAttendanceHistory(final Pageable pageable) {
        User user = userContextProvider.getCurrentUserEntity();

        return attendanceRecordRepository
                .findByUser(user, pageable)
                .map(attendanceResponseMapper::fromEntity);
    }

    @Override
    public ManagerStatsResponse getManagerStats(
            final String departmentId,
            final LocalDate startDate,
            final LocalDate endDate) {
        return getManagerStats(
                departmentId,
                startDate,
                endDate,
                null);
    }

    public ManagerStatsResponse getManagerStats(
            final String departmentId,
            final LocalDate startDate,
            final LocalDate endDate,
            final AttendanceStatus status) {
        List<User> employees = userContextProvider.getEmployeesByDepartment(departmentId);
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

        List<Long> employeeIds = employees.stream().map(User::getId).toList();

        List<AttendanceRecord> records;
        if (startDate != null && endDate != null) {
            records = attendanceRecordRepository.findByUserIdInAndAttendanceDateBetween(employeeIds, startDate,
                    endDate);
        } else {
            records = attendanceRecordRepository.findByUserIdIn(employeeIds);
        }

        if (status != null) {
            records = records.stream()
                    .filter(r -> r.getStatus() == status)
                    .toList();
        }
        long checkedIn = records.stream()
                .filter(r -> r.getCheckInTime() != null)
                .count();

        long checkedOut = records.stream()
                .filter(r -> r.getCheckOutTime() != null)
                .count();

        long lateArrivals = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.LATE)
                .count();

        long absent = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT)
                .count();

        return ManagerStatsResponse.builder()
                .totalEmployees(totalEmployees)
                .checkedIn(checkedIn)
                .checkedOut(checkedOut)
                .lateArrivals(lateArrivals)
                .absent(absent)
                .build();
    }

    @Override
    public List<AttendanceResponse> getManagerAttendanceList(
            final LocalDate startDate,
            final LocalDate endDate) {
        return getManagerAttendanceList(startDate, endDate, null);
    }

    public List<AttendanceResponse> getManagerAttendanceList(
            final LocalDate startDate,
            final LocalDate endDate,
            final AttendanceStatus status) {

        DateRange dateRange = getDateRange(startDate, endDate);

        List<AttendanceRecord> records = getFilteredRecords(dateRange.startDate(), dateRange.endDate(), status);

        return records.stream()
                .map(attendanceResponseMapper::fromEntity)
                .sorted((a, b) -> {
                    if (a.attendanceDate() == null && b.attendanceDate() == null)
                        return 0;
                    if (a.attendanceDate() == null)
                        return 1;
                    if (b.attendanceDate() == null)
                        return -1;
                    return b.attendanceDate().compareTo(a.attendanceDate());
                })
                .toList();
    }

    @Override
    public Page<AttendanceResponse> getAttendanceHistory(
            final LocalDate startDate,
            final LocalDate endDate,
            final AttendanceStatus status,
            final Pageable pageable) {
        User user = userContextProvider.getCurrentUserEntity();
        return attendanceRecordRepository
                .findFilteredAttendanceHistory(user, startDate, endDate, status, pageable)
                .map(attendanceResponseMapper::fromEntity);
    }

    @Override
    public AttendanceHistoryStatsResponse getAttendanceHistoryStats() {
        User user = userContextProvider.getCurrentUserEntity();
        return computeStats(user, null, null, null);
    }

    @Override
    public AttendanceHistoryStatsResponse getAttendanceHistoryStats(
            final LocalDate startDate,
            final LocalDate endDate,
            final AttendanceStatus status) {
        User user = userContextProvider.getCurrentUserEntity();
        return computeStats(user, startDate, endDate, status);
    }

    @Override
    public Optional<AttendanceRecord> getTodayAttendanceRecord(final User user) {
        if (user == null) {
            return Optional.empty();
        }
        LocalDate today = todayDate();
        return attendanceRecordRepository.findByUserAndAttendanceDateWithLock(user, today);
    }

    private LocalDate todayDate() {
        return LocalDate.now(applicationZoneId);
    }

    private DateRange getDateRange(
            LocalDate startDate,
            LocalDate endDate) {
        LocalDate today = todayDate();

        startDate = startDate == null
                ? (endDate == null ? today.minusMonths(1) : endDate)
                : startDate;

        endDate = endDate == null
                ? today
                : endDate;
        return new DateRange(startDate, endDate);
    }

    private List<AttendanceRecord> getFilteredRecords(
            final LocalDate startDate,
            final LocalDate endDate,
            final AttendanceStatus status) {
        if (status != null) {
            return attendanceRecordRepository.findByAttendanceDateBetweenAndStatus(startDate, endDate, status);
        }
        return attendanceRecordRepository.findByAttendanceDateBetween(startDate, endDate);
    }

    private AttendanceHistoryStatsResponse computeStats(
            final User user,
            final LocalDate startDate,
            final LocalDate endDate,
            final AttendanceStatus status) {
        List<AttendanceRecord> records;

        if (startDate != null && endDate != null) {
            records = attendanceRecordRepository.findByUserAndAttendanceDateBetween(user, startDate, endDate);
        } else {
            records = attendanceRecordRepository.findAllByUser(user);
        }

        if (status != null) {
            records = records.stream()
                    .filter(r -> r.getStatus() == status)
                    .toList();
        }

        long totalDays = records.size();
        long onTime = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .count();
        long late = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.LATE)
                .count();
        long earlyLeave = 0;
        long totalWorkingMinutes = 0;

        for (AttendanceRecord record : records) {
            if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
                totalWorkingMinutes += attendanceCalculator.workingMinutes(record.getCheckInTime(),
                        record.getCheckOutTime());
            }

            if (attendanceCalculator.isEarlyLeave(record.getCheckOutTime())) {
                earlyLeave++;
            }
        }

        return AttendanceHistoryStatsResponse.builder()
                .totalDays(totalDays)
                .onTime(onTime)
                .late(late)
                .earlyLeave(earlyLeave)
                .totalWorkingMinutes(totalWorkingMinutes)
                .build();
    }

}