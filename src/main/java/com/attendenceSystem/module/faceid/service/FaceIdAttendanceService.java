package com.attendenceSystem.module.faceid.service;

import com.attendenceSystem.module.faceid.dto.request.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.response.FaceIdAttendanceResponse;

public interface FaceIdAttendanceService {
    FaceIdAttendanceResponse processAttendance(FaceIdAttendanceRequest request);
}