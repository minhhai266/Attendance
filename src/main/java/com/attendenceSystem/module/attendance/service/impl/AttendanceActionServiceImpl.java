package com.attendenceSystem.module.attendance.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedInException;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedOutException;
import com.attendenceSystem.module.attendance.exception.InvalidAttendanceStateException;
import com.attendenceSystem.module.attendance.exception.NotCheckedInException;
import com.attendenceSystem.module.attendance.mapper.response.AttendanceResponseMapper;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.service.AttendanceActionService;
import com.attendenceSystem.module.attendance.util.AttendanceCalculator;
import com.attendenceSystem.module.schedule.entity.WorkSchedule;
import com.attendenceSystem.module.schedule.repository.WorkScheduleRepository;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.provider.UserContextProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(
    noRollbackFor = {
        AlreadyCheckedInException.class,
        AlreadyCheckedOutException.class,
        InvalidAttendanceStateException.class,
        NotCheckedInException.class
    }
)
public class AttendanceActionServiceImpl implements AttendanceActionService {
    private static final Long DEFAULT_SCHEDULE_ID = 1L;
    private static final String DAY_OFF_NOTE = "Ngày nghỉ";

    private final AttendanceCalculator attendanceCalculator;
    private final UserContextProvider userContextProvider;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceResponseMapper attendanceResponseMapper;
    private final WorkScheduleRepository workScheduleRepository;

    private final ZoneId applicationZoneId;


    @Override
    public AttendanceResponse checkIn() {
        return checkIn(userContextProvider.getCurrentUserEntity());
    }


    @Override
    public AttendanceResponse checkOut() {
        return checkOut(userContextProvider.getCurrentUserEntity());
    }


    @Override
    public AttendanceResponse checkIn(final User user) {
        LocalDate today = todayDate();
        Optional<AttendanceRecord> existing = attendanceRecordRepository
                .findByUserAndAttendanceDateWithLock(user, today);
        if (existing.isPresent()) {
            throw new AlreadyCheckedInException("Bạn đã điểm danh hôm nay");
        }
        LocalDateTime checkInTime = LocalDateTime.now(applicationZoneId);

        boolean isWorkingDay = isWorkingDay(today);

        if (isWorkingDay && attendanceCalculator.isPastAllowedCheckInTime(checkInTime)) {
            throw new InvalidAttendanceStateException("Đã quá thời gian ca làm, bạn sẽ bị tính là vắng măt ngày hôm nay");
        }

        AttendanceStatus status;
        String note;
        if (!isWorkingDay) {
            status = AttendanceStatus.DAY_OFF;
            note = DAY_OFF_NOTE;
        } else {
            boolean late = attendanceCalculator.isLate(checkInTime);
            status = late ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
            note = late ? "Đi muộn" : null;
        }

        AttendanceRecord attendanceRecord = AttendanceRecord.builder()
                .user(user)
                .attendanceDate(today)
                .checkInTime(checkInTime)
                .status(status)
                .note(note)
                .build();
        attendanceRecordRepository.save(attendanceRecord);
        return attendanceResponseMapper.fromEntity(attendanceRecord);
    }

    @Override
    public AttendanceResponse checkOut(final User user) {
        LocalDate today = todayDate();
        AttendanceRecord attendance = attendanceRecordRepository
                .findByUserAndAttendanceDateWithLock(user, today)
                .orElseThrow(() -> new NotCheckedInException(
                        "Bạn không điểm danh hôm nay nên không thể checkout"));
        if (attendance.getCheckOutTime() != null) {
            throw new AlreadyCheckedOutException("Bạn đã checkout rồi");
        }
        if (attendance.getStatus() != AttendanceStatus.PRESENT
                && attendance.getStatus() != AttendanceStatus.LATE
                && attendance.getStatus() != AttendanceStatus.DAY_OFF) {
            throw new InvalidAttendanceStateException("Trạng thái điểm danh không hợp lệ");
        }
        LocalDateTime checkOutTime = LocalDateTime.now(applicationZoneId);
        if (checkOutTime.isBefore(attendance.getCheckInTime())) {
            throw new InvalidAttendanceStateException("Thời gian checkout phải sau thời gian check-in");
        }

        boolean isDayOff = attendance.getStatus() == AttendanceStatus.DAY_OFF;

        if (!isDayOff && attendanceCalculator.isBeforeMinCheckOutTime(checkOutTime)) {
            throw new InvalidAttendanceStateException("Chưa đến thời gian tối thiểu để checkout");
        }

        boolean earlyLeave = !isDayOff && attendanceCalculator.isEarlyLeave(checkOutTime);
        attendance.setCheckOutTime(checkOutTime);
        if (earlyLeave) {
            attendance.setNote((attendance.getNote() != null ? attendance.getNote() + "; " : "") + "Về sớm");
        }
        attendanceRecordRepository.save(attendance);
        return attendanceResponseMapper.fromEntity(attendance);
    }

    private boolean isWorkingDay(LocalDate date) {
        WorkSchedule schedule = workScheduleRepository.findById(DEFAULT_SCHEDULE_ID).orElse(null);
        if (schedule == null || schedule.getWorkingDays() == null || schedule.getWorkingDays().isEmpty()) {
            // Không có schedule → mặc định là working day (giữ hành vi cũ)
            return true;
        }
        Set<DayOfWeek> workingDays = schedule.getWorkingDays();
        return workingDays.contains(date.getDayOfWeek());
    }

    private LocalDate todayDate() {
        return LocalDate.now(applicationZoneId);
    }
}