package com.attendenceSystem.module.faceid.dto;

import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceIdAttendanceResponse {
    private boolean success;
    private FaceIdAction action;
    private String message;
    private String trackingId;
    private Instant timestamp;
    private AttendanceResponse attendance;
}