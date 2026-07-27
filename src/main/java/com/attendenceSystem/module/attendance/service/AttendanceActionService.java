package com.attendenceSystem.module.attendance.service;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.user.entity.User;

public interface AttendanceActionService {
    AttendanceResponse checkIn();

    AttendanceResponse checkOut();

    AttendanceResponse checkIn(User user);

    AttendanceResponse checkOut(User user);
}
