package com.attendenceSystem.module.attendance.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.service.AttendanceScheduleService;
import com.attendenceSystem.module.schedule.entity.WorkSchedule;
import com.attendenceSystem.module.schedule.repository.WorkScheduleRepository;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.provider.UserContextProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceScheduleServiceImpl implements AttendanceScheduleService {
    private static final Long DEFAULT_SCHEDULE_ID = 1L;

    private final UserContextProvider userContextProvider;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final ZoneId applicationZoneId;

    private static final String AUTO_ABSENT_NOTE = "Hệ thống tự động đánh vắng do không check-in";
    private static final String ON_LEAVE_NOTE = "Nghỉ phép đã được phê duyệt";
    private static final String FORGOT_CHECKOUT_NOTE = "Quên check-out; Coi như vắng mặt";
    private static final String DAY_OFF_NO_CHECKOUT_NOTE = "Ngày nghỉ; Không checkout";

    @Scheduled(cron = "0 15 17 * * *", zone = "${app.timezone:Asia/Ho_Chi_Minh}")
    @Transactional
    @Override
    public void autoMarkAbsent() {
        LocalDate today = LocalDate.now(applicationZoneId);

        if (!isWorkingDay(today)) {
            log.info("Hôm nay ({}) không phải ngày làm việc, bỏ qua đánh vắng tự động.", today);
            return;
        }

        log.info("Bắt đầu chạy tiến trình quét vắng mặt tự động cho ngày: {}", today);

        List<User> absentUsers = userContextProvider.getUsersWithoutAttendanceForDate(today);

        if (absentUsers.isEmpty()) {
            log.info("Không có nhân viên vắng mặt vào ngày hôm nay");
            return;
        }

        List<Long> onLeaveUserIds = leaveRequestRepository.findUserIdsOnLeaveForDate(today, LeaveStatus.APPROVED);

        List<AttendanceRecord> absentRecords = absentUsers
                .stream()
                .map(user -> {
                    if (onLeaveUserIds.contains(user.getId())) {
                        return AttendanceRecord.builder()
                                .user(user)
                                .attendanceDate(today)
                                .status(AttendanceStatus.LEAVE)
                                .note(ON_LEAVE_NOTE)
                                .build();
                    }
                    return AttendanceRecord.builder()
                            .user(user)
                            .attendanceDate(today)
                            .status(AttendanceStatus.ABSENT)
                            .note(AUTO_ABSENT_NOTE)
                            .build();
                })
                .toList();
        attendanceRecordRepository.saveAll(absentRecords);
        log.info("Đã đánh vắng tự động cho {} nhân viên ({} nghỉ phép).", absentUsers.size(), onLeaveUserIds.size());
    }

    @Scheduled(cron = "0 50 23 * * *",
            zone = "${app.timezone:Asia/Ho_Chi_Minh}")
    @Transactional
    @Override
    public void autoHandleMissingCheckOut() {
        LocalDate today = LocalDate.now(applicationZoneId);

        log.info("Bắt đầu quét các bản ghi quên check-out cho ngày: {}", today);

        List<AttendanceRecord> missingCheckOutRecords = attendanceRecordRepository.findRecordsMissingCheckOut(today);

        if (missingCheckOutRecords.isEmpty()) {
            log.info("Tất cả nhân viên đều đã check-out hôm nay.");
            return;
        }

        List<Long> onLeaveUserIds = leaveRequestRepository.findUserIdsOnLeaveForDate(today, LeaveStatus.APPROVED);
        boolean isWorkingDay = isWorkingDay(today);

        for (AttendanceRecord record : missingCheckOutRecords) {
            // Bỏ qua những user đang trong ngày nghỉ phép đã được duyệt
            if (onLeaveUserIds.contains(record.getUser().getId())) {
                continue;
            }
            String currentNote = record.getNote() != null ? record.getNote() + "; " : "";
            if (isWorkingDay) {
                // Working day không checkout → coi như vắng mặt (giữ nguyên hành vi cũ)
                record.setStatus(AttendanceStatus.ABSENT);
                record.setNote(currentNote + FORGOT_CHECKOUT_NOTE);
            } else {
                // Ngày nghỉ không checkout → DAY_OFF + ghi chú
                record.setStatus(AttendanceStatus.DAY_OFF);
                record.setNote(currentNote + DAY_OFF_NO_CHECKOUT_NOTE);
            }
        }

        attendanceRecordRepository.saveAll(missingCheckOutRecords);
        log.info("Đã xử lý {} trường hợp quên check-out.", missingCheckOutRecords.size());
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
}