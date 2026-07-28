package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.entity.AttendanceRecord;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.repository.AttendanceRecordRepository;
import com.attendenceSystem.module.attendance.service.AttendanceScheduleService;
import com.attendenceSystem.module.attendance.util.TimeZoneProvider;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.provider.UserContextProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceScheduleServiceImpl implements AttendanceScheduleService {
    private final UserContextProvider userContextProvider;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TimeZoneProvider timeZoneProvider;

    private static final String AUTO_ABSENT_NOTE = "Hệ thống tự động đánh vắng do không check-in";
    private static final String FORGOT_CHECKOUT_NOTE = "Quên check-out";

    @Scheduled(cron = "0 15 17 * * MON-FRI", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    @Override
    public void autoMarkAbsent() {
        LocalDate today = LocalDate.now(timeZoneProvider.getZoneId());

        log.info("Bắt đầu chạy tiến trình quét vắng mặt tự động cho ngày: {}", today);

        List<User> absentUsers = userContextProvider.getUsersWithoutAttendanceForDate(today);

        if (absentUsers.isEmpty()) {
            log.info("Không có nhân viên vắng mặt vào ngày hôm nay");
            return;
        }
        List<AttendanceRecord> absentRecords = absentUsers
                .stream()
                .map(user -> AttendanceRecord.builder()
                        .user(user)
                        .attendanceDate(today)
                        .status(AttendanceStatus.ABSENT)
                        .note(AUTO_ABSENT_NOTE)
                        .build())
                .toList();
        attendanceRecordRepository.saveAll(absentRecords);
        log.info("Đã đánh vắng tự động cho {} nhân viên.", absentUsers.size());
    }

    @Scheduled(cron = "0 50 23 * * MON-FRI",
            zone = "Asia/Ho_Chi_Minh")
    @Transactional
    @Override
    public void autoHandleMissingCheckOut() {
        LocalDate today = LocalDate.now(timeZoneProvider.getZoneId());

        log.info("Bắt đầu quét các bản ghi quên check-out cho ngày: {}", today);

        List<AttendanceRecord> missingCheckOutRecords = attendanceRecordRepository.findRecordsMissingCheckOut(today);

        if(missingCheckOutRecords.isEmpty()){
            log.info("Tất cả nhân viên đều đã check-out hôm nay.");
            return;
        }

        for(AttendanceRecord record : missingCheckOutRecords){
            String currentNote = record.getNote() != null ? record.getNote() + "; " : "";
            record.setNote(currentNote + FORGOT_CHECKOUT_NOTE);
        }

        attendanceRecordRepository.saveAll(missingCheckOutRecords);
        log.info("Đã xử lý {} trường hợp quên check-out.", missingCheckOutRecords.size());
    }

}
