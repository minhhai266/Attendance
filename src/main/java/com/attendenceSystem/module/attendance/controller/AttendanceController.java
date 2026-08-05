package com.attendenceSystem.module.attendance.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.AttendanceHistoryStatsResponse;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.entity.enums.AttendanceStatus;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedInException;
import com.attendenceSystem.module.attendance.exception.AlreadyCheckedOutException;
import com.attendenceSystem.module.attendance.exception.InvalidAttendanceStateException;
import com.attendenceSystem.module.attendance.exception.NotCheckedInException;
import com.attendenceSystem.module.attendance.service.AttendanceActionService;
import com.attendenceSystem.module.attendance.service.AttendanceService;
import com.attendenceSystem.module.attendance.service.LeaveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(Routes.Attendance.ROOT)
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final AttendanceActionService attendanceActionService;

    @GetMapping
    public String toAttendanceListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("attendanceHistory", attendanceService.getAttendanceHistory(pageable));
        return Views.Attendance.LIST;
    }

    @GetMapping(Routes.Attendance.CHECK)
    public String toAttendanceCheckPage() {
        return Views.Attendance.CHECK;
    }

    @PostMapping(Routes.Attendance.CHECK_IN)
    public String checkIn(RedirectAttributes redirectAttributes) {
        AttendanceResponse attendance = attendanceActionService.checkIn();
        redirectAttributes.addFlashAttribute("successMessage", "Điểm danh thành công cho " + attendance.fullName());
        return Views.Attendance.LIST;
    }

    @PostMapping(Routes.Attendance.CHECK_OUT)
    public String checkOut(RedirectAttributes redirectAttributes) {
        AttendanceResponse attendance = attendanceActionService.checkOut();
        redirectAttributes.addFlashAttribute("successMessage", "Checkout thành công cho " + attendance.fullName());
        return Views.Attendance.LIST;
    }

    @GetMapping(Routes.Attendance.HISTORY)
    public String attendanceHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        boolean hasFilter = startDate != null || endDate != null || status != null;

        Page<AttendanceResponse> attendanceHistory;
        AttendanceHistoryStatsResponse stats;

        if (hasFilter) {
            attendanceHistory = attendanceService.getAttendanceHistory(startDate, endDate, status, pageable);
            stats = attendanceService.getAttendanceHistoryStats(startDate, endDate, status);
        } else {
            attendanceHistory = attendanceService.getAttendanceHistory(pageable);
            stats = attendanceService.getAttendanceHistoryStats();
        }

        model.addAttribute("attendanceHistory", attendanceHistory);
        model.addAttribute("stats", stats);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("hasFilter", hasFilter);

        return Views.Attendance.HISTORY;
    }

    @GetMapping(Routes.Attendance.LEAVE)
    public String leaveRequestList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) String week,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        boolean hasFilter = (keyword != null && !keyword.isBlank()) || status != null || (week != null && !week.isBlank());

        Page<LeaveRequestResponse> leaveRequests;
        if (hasFilter) {
            leaveRequests = leaveService.getAllLeaveRequests(keyword, status, week, pageable);
        } else {
            leaveRequests = leaveService.getAllLeaveRequests(pageable);
        }

        model.addAttribute("leaveRequests", leaveRequests);
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedWeek", week);
        model.addAttribute("hasFilter", hasFilter);

        return Views.Attendance.LEAVE_LIST;
    }

    @GetMapping(Routes.Attendance.MY_LEAVE)
    public String myLeaveRequestList(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) String week,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        boolean hasFilter = status != null || (week != null && !week.isBlank());

        Page<LeaveRequestResponse> leaveRequests;
        if (hasFilter) {
            leaveRequests = leaveService.getLeaveRequests(status, week, pageable);
        } else {
            leaveRequests = leaveService.getLeaveRequests(pageable);
        }

        model.addAttribute("leaveRequests", leaveRequests);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedWeek", week);
        model.addAttribute("hasFilter", hasFilter);

        return Views.Attendance.MY_LEAVE_LIST;
    }

    @GetMapping(Routes.Attendance.LEAVE + Routes.Action.CREATE)
    public String toLeaveRequestPage(Model model) {
        model.addAttribute("createLeaveRequest", new CreateLeaveRequest());
        return Views.Attendance.LEAVE_CREATE;
    }

    @PostMapping(Routes.Attendance.LEAVE + Routes.Action.CREATE)
    public String createLeaveRequest(
            @Valid @ModelAttribute CreateLeaveRequest createLeaveRequest,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("createLeaveRequest", createLeaveRequest);
            return Views.Attendance.LEAVE_CREATE;
        }
        leaveService.createLeaveRequest(createLeaveRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu nghỉ phép đã được gửi.");
        return Routes.REDIRECT + Routes.Attendance.ROOT + Routes.Attendance.LEAVE + Routes.Action.CREATE;
    }

    @PostMapping(Routes.Attendance.LEAVE + Routes.Action.ACCEPT + "/{id}")
    public String acceptLeaveRequest(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        leaveService.acceptLeave(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đơn nghỉ phép.");
        return Routes.REDIRECT + Routes.Attendance.ROOT + Routes.Attendance.LEAVE;
    }

    @PostMapping(Routes.Attendance.LEAVE + Routes.Action.REJECT + "/{id}")
    public String rejectLeaveRequest(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        leaveService.rejectLeave(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đơn nghỉ phép.");
        return Routes.REDIRECT + Routes.Attendance.ROOT + Routes.Attendance.LEAVE;
    }

    @ExceptionHandler({
            AlreadyCheckedInException.class,
            AlreadyCheckedOutException.class,
            NotCheckedInException.class,
            InvalidAttendanceStateException.class,
            IllegalArgumentException.class
    })
    public String handleBadRequest(RuntimeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return Views.Attendance.LIST;
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedError(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Unexpected error in AttendanceController", ex);
        redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra, vui lòng thử lại sau");
        return Views.Attendance.LIST;
    }
}
