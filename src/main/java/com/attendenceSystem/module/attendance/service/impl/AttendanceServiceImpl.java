package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerStatsResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedInException;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedOutException;
import com.attendenceSystem.module.attendance.exception.InvalidAttendanceStateException;
import com.attendenceSystem.module.attendance.exception.NotCheckedInException;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.model.DateRange;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.attendance.util.TimeZoneProvider;
import com.attendenceSystem.module.user.entity.User;

import com.attendenceSystem.module.user.provider.UserContextProvider;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceResponseMapper attendanceResponseMapper;
    private final AttendanceCalculator attendanceCalculator;
    private final TimeZoneProvider timeZoneProvider;
    private final UserContextProvider userContextProvider;

    @Value("${attendance.start-work:08:00}")
    private String startWork;

    @Value("${attendance.end-work:17:00}")
    private String endWork;

    @Transactional
    @Override
    public AttendanceResponse checkIn() {
        return checkIn(userContextProvider.getCurrentUserEntity());
    }

    @Transactional
    @Override
    public AttendanceResponse checkOut() {
        return checkOut(userContextProvider.getCurrentUserEntity());
    }

    @Transactional
    @Override
    public AttendanceResponse checkIn(User user) {
        LocalDate today = todayDate();
        try {
            Optional<AttendanceRecord> existing = attendanceRecordRepository
                    .findByUserAndAttendanceDateWithLock(user, today);
            if (existing.isPresent()) {
                throw new AlreadyCheckedInException("Bạn đã điểm danh hôm nay");
            }
            LocalDateTime checkInTime = LocalDateTime.now();
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
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyCheckedInException("Bạn đã điểm danh hôm nay");
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new AlreadyCheckedInException("Dữ liệu đã bị thay đổi, vui lòng thử lại");
        }
    }

    @Transactional
    @Override
    public AttendanceResponse checkOut(User user) {
        LocalDate today = todayDate();
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
        LocalDateTime checkOutTime = LocalDateTime.now();
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
    public Page<AttendanceResponse> getAttendanceHistory(final Pageable pageable) {
        User user = userContextProvider.getCurrentUserEntity();

        return attendanceRecordRepository
                .findByUser(user, pageable)
                .map(attendanceResponseMapper::fromEntity);
    }

    @Override
    public ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate) {
        return getManagerStats(departmentId, startDate, endDate, null);
    }

    public ManagerStatsResponse getManagerStats(String departmentId, LocalDate startDate, LocalDate endDate,
            AttendanceStatus status) {
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

        List<AttendanceRecord> records = getFilteredRecords(dateRange.startDate(), dateRange.endDate(), status);

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
            if (record == null){
                absent++;
                continue;
            }
            checkedIn++;
            if(record.getStatus() == AttendanceStatus.LATE) {
                lateArrivals++;
            }
            if (record.getCheckOutTime() !=null){
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
    public List<AttendanceResponse> getManagerAttendanceList(LocalDate startDate,
            LocalDate endDate) {
        return getManagerAttendanceList(startDate, endDate, null);
    }

    public List<AttendanceResponse> getManagerAttendanceList(LocalDate startDate,
            LocalDate endDate, AttendanceStatus status) {
                
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
    public Optional<AttendanceRecord> getTodayAttendanceRecord(User user) {
        if (user == null) {
            return Optional.empty();
        }
        LocalDate today = todayDate();
        return attendanceRecordRepository.findByUserAndAttendanceDateWithLock(user, today);
    }

    private LocalDate todayDate() {
        return LocalDate.now(timeZoneProvider.getZoneId());
    }

    private DateRange getDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate today = todayDate();

        startDate = startDate == null
        ? (endDate == null ? today.minusMonths(1) : endDate) : startDate;

        endDate = endDate == null
        ? today : endDate;
        return new DateRange(startDate, endDate);
    }

    private List<AttendanceRecord> getFilteredRecords(LocalDate startDate, LocalDate endDate, AttendanceStatus status) {
        if (status != null) {
            return attendanceRecordRepository.findByAttendanceDateBetweenAndStatus(startDate, endDate, status);
        }
        return attendanceRecordRepository.findByAttendanceDateBetween(startDate, endDate);
    }

}