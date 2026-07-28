package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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


    @Scheduled(cron = "0 15 17 * * MON-FRI")
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
        List<AttendanceRecord> absentRecords = absentUsers.stream()
                .map(user -> AttendanceRecord.builder()
                        .user(user)
                        .attendanceDate(today)
                        .status(AttendanceStatus.ABSENT)
                        .note("Hệ thống tự động đánh vắng do không check-in")
                        .build())
                .collect(Collectors.toList());
        attendanceRecordRepository.saveAll(absentRecords);
        log.info("Đã đánh vắng tự động cho {} nhân viên.", absentUsers.size());
    }

}
