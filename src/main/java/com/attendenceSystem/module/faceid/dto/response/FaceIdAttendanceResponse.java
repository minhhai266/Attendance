package com.attendenceSystem.module.faceid.dto.response;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.faceid.dto.FaceIdAction;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FaceIdAttendanceResponse(
        boolean success,
        FaceIdAction action,
        String message,
        String trackingId,
        LocalDateTime timestamp,
        AttendanceResponse attendance
) {
}