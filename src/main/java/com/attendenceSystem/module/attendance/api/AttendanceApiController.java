package com.attendenceSystem.module.attendance.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Routes.API + Routes.Attendance.ROOT)
@RequiredArgsConstructor
public class AttendanceApiController {

    private final AttendanceService attendanceService;

    @PostMapping(Routes.Attendance.CHECK_IN)
    public ResponseEntity<AttendanceResponse> checkIn() {
        AttendanceResponse resp = attendanceService.checkIn();
        return ResponseEntity.ok(resp);
    }

    @PostMapping(Routes.Attendance.CHECK_OUT)
    public ResponseEntity<AttendanceResponse> checkOut() {
        AttendanceResponse resp = attendanceService.checkOut();
        return ResponseEntity.ok(resp);
    }

    @GetMapping(Routes.Attendance.HISTORY)
    public ResponseEntity<Page<AttendanceResponse>> getHistory(Pageable pageable) {
        Page<AttendanceResponse> page = attendanceService.getAttendanceHistory(pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping(Routes.Attendance.LEAVE)
    public ResponseEntity<LeaveRequestResponse> createLeave(@RequestBody CreateLeaveRequest request) {
        LeaveRequestResponse resp = attendanceService.createLeaveRequest(request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/leaves")
    public ResponseEntity<Page<LeaveRequestResponse>> getLeaves(Pageable pageable) {
        Page<LeaveRequestResponse> page = attendanceService.getLeaveRequests(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping(Routes.Attendance.LEAVE_DETAIL + "/{id}")
    public ResponseEntity<LeaveDetailResponse> getLeaveDetail(@PathVariable Long id) {
        LeaveDetailResponse detail = attendanceService.getLeaveDetail(id);
        return ResponseEntity.ok(detail);
    }
}
