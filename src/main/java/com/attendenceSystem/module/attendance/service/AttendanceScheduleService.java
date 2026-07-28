package com.attendenceSystem.module.attendance.service;

public interface AttendanceScheduleService {
    void autoMarkAbsent();

    void autoHandleMissingCheckOut();
}
