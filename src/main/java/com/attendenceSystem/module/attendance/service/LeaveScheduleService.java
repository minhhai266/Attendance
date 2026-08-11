package com.attendenceSystem.module.attendance.service;

import java.time.LocalDate;

public interface LeaveScheduleService {
    void validateBlackoutPeriod(LocalDate startDate);
    void autoRejectExpiredLeaveRequests();
}