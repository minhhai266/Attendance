package com.attendenceSystem.module.faceid.service;

import java.util.List;

import com.attendenceSystem.module.faceid.dto.request.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.request.FaceIdManualAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.response.EmployeeDirectoryResponse;
import com.attendenceSystem.module.faceid.dto.response.FaceIdAttendanceResponse;
import com.attendenceSystem.module.faceid.dto.response.FaceIdentifyResponse;

public interface FaceIdAttendanceService {
    FaceIdAttendanceResponse processAttendance(FaceIdAttendanceRequest request);

    FaceIdentifyResponse identify(String imageBase64);

    FaceIdAttendanceResponse processManualAttendance(FaceIdManualAttendanceRequest request);

    List<EmployeeDirectoryResponse> listEmployeesForManualAttendance();

}