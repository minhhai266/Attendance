package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.config.SystemConfig;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.service.LeaveScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveScheduleServiceImpl implements LeaveScheduleService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final SystemConfig systemConfig;

    private static final LocalTime DEFAULT_AUTO_REJECT_TIME = LocalTime.of(22, 0);

    @Override
    public void validateBlackoutPeriod(final LocalDate startDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        int blackoutMinutes = systemConfig.get().getLeaveBlackoutMinutes() != null
                ? systemConfig.get().getLeaveBlackoutMinutes()
                : 2;
        LocalTime autoReject = DEFAULT_AUTO_REJECT_TIME;
        LocalTime blackoutStart = autoReject.minusMinutes(blackoutMinutes);
        LocalTime blackoutEnd = autoReject.plusMinutes(blackoutMinutes);

        boolean isInBlackoutPeriod = currentTime.isAfter(blackoutStart) && currentTime.isBefore(blackoutEnd);
        boolean isAfterRejectTime = currentTime.isAfter(autoReject);
        boolean isBeforeMidnight = currentTime.isBefore(LocalTime.of(23, 59));

        if (isInBlackoutPeriod) {
            throw new IllegalStateException(
                    "Không thể tạo đơn xin nghỉ trong khoảng thời gian " + blackoutStart + " - " + blackoutEnd);
        }

        if (isAfterRejectTime && isBeforeMidnight && startDate.equals(tomorrow)) {
            throw new IllegalStateException(
                    "Không thể tạo đơn xin nghỉ cho ngày mai sau thời gian " + autoReject);
        }
    }

    @Transactional
    public void autoRejectExpiredLeaveRequests() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Running auto-reject task for leave requests with startDate <= {}", tomorrow);

        var expiredLeaves = leaveRequestRepository.findByStatusAndStartDateLessThanEqual(LeaveStatus.PENDING, tomorrow);

        if (expiredLeaves.isEmpty()) {
            log.info("No expired leave requests found");
            return;
        }

        log.info("Found {} expired leave requests to reject", expiredLeaves.size());

        for (LeaveRequest leave : expiredLeaves) {
            leave.setStatus(LeaveStatus.REJECTED);
            leaveRequestRepository.save(leave);
            log.info("Auto-rejected leave request ID: {} for user: {}, startDate: {}",
                    leave.getId(), leave.getUser().getId(), leave.getStartDate());
        }

        log.info("Auto-reject task completed successfully");
    }
}