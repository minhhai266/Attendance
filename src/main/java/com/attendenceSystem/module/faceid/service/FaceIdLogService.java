package com.attendenceSystem.module.faceid.service;

import java.time.LocalDateTime;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.faceid.dto.request.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.entity.enums.FaceIdAction;

public interface FaceIdLogService {
    void saveRecognitionLog(
            FaceIdAttendanceRequest request,
            FaceIdAction action,
            String message,
            AttendanceResponse attendance,
            LocalDateTime timestamp);
}
