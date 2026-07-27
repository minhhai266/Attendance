package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.model.DateRange;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.attendance.util.TimeZoneProvider;
import com.attendenceSystem.module.user.entity.User;

import com.attendenceSystem.module.user.provider.UserContextProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceResponseMapper attendanceResponseMapper;
    
    private final TimeZoneProvider timeZoneProvider;
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
        DateRange dateRange = getDateRange(startDate, endDate);

        List<AttendanceRecord> records = getFilteredRecords(
                dateRange.startDate(),
                dateRange.endDate(),
                status);

        long checkedIn = 0;
        long checkedOut = 0;
        long lateArrivals = 0;
        long absent = 0;

        Map<Long, AttendanceRecord> attendanceMap = records.stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.toMap(
                        r -> r.getUser().getId(),
                        Function.identity(),
                        (a, b) -> a));
        for (User emp : employees) {
            AttendanceRecord record = attendanceMap.get(emp.getId());
            if (record == null) {
                absent++;
                continue;
            }
            checkedIn++;
            if (record.getStatus() == AttendanceStatus.LATE) {
                lateArrivals++;
            }
            if (record.getCheckOutTime() != null) {
                checkedOut++;
            }
        }
        //
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
    public Optional<AttendanceRecord> getTodayAttendanceRecord(final User user) {
        if (user == null) {
            return Optional.empty();
        }
        LocalDate today = todayDate();
        return attendanceRecordRepository.findByUserAndAttendanceDateWithLock(user, today);
    }

    private LocalDate todayDate() {
        return LocalDate.now(timeZoneProvider.getZoneId());
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

}