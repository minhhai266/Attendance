package com.attendenceSystem.module.attendance.service;

public interface LeaveService {
    void acceptLeave(Long id);
    void rejectLeave(Long id);
}
