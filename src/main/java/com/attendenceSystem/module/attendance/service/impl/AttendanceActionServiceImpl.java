package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
import com.attendenceSystem.module.attendance.util.TimeZoneProvider;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.provider.UserContextProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceActionServiceImpl implements AttendanceActionService {
    private final AttendanceCalculator attendanceCalculator;
    private final UserContextProvider userContextProvider;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceResponseMapper attendanceResponseMapper;

    private final TimeZoneProvider timeZoneProvider;

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
    public AttendanceResponse checkIn(final User user) {
        LocalDate today = todayDate();
        try {
            Optional<AttendanceRecord> existing = attendanceRecordRepository
                    .findByUserAndAttendanceDateWithLock(user, today);
            if (existing.isPresent()) {
                throw new AlreadyCheckedInException("Bạn đã điểm danh hôm nay");
            }
            LocalDateTime checkInTime = LocalDateTime.now();

            if (attendanceCalculator.isPastAllowedCheckInTime(checkInTime)) {
                throw new InvalidAttendanceStateException("Đã quá thời gian ca làm, bạn tính là vắng măt ngày hôm nay");
            }

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
    public AttendanceResponse checkOut(final User user) {
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

    private LocalDate todayDate() {
        return LocalDate.now(timeZoneProvider.getZoneId());
    }
}
