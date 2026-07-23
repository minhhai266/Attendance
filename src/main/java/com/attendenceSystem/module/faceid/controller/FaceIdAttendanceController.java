package com.attendenceSystem.module.faceid.controller;

import com.attendenceSystem.module.faceid.dto.FaceIdAttendanceRequest;
import com.attendenceSystem.module.faceid.dto.FaceIdAttendanceResponse;
import com.attendenceSystem.module.faceid.service.FaceIdAttendanceService;
import com.attendenceSystem.module.faceid.service.impl.FaceIdAttendanceServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/face-id")
@RequiredArgsConstructor
public class FaceIdAttendanceController {

    private final FaceIdAttendanceServiceImpl faceIdAttendanceService;

    @PostMapping("/attendance")
    public ResponseEntity<FaceIdAttendanceResponse> processAttendance(
            @Valid @RequestBody FaceIdAttendanceRequest request) {

        FaceIdAttendanceResponse response = faceIdAttendanceService.processAttendance(request);
        return ResponseEntity.ok(response);
    }
}