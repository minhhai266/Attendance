package com.attendenceSystem.module.attendance.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;
import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.AttendanceResponse;
import com.attendenceSystem.module.attendance.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(Routes.Attendance.ROOT)
public class AttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping
    public String toAttendanceListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("attendanceHistory", attendanceService.getAttendanceHistory(pageable));
        return Views.Attendance.LIST;
    }

    @PostMapping(Routes.Attendance.CHECK_IN)
    public String checkIn(RedirectAttributes redirectAttributes) {
        try {
            AttendanceResponse attendance = attendanceService.checkIn();
            redirectAttributes.addFlashAttribute("successMessage", "Điểm danh thành công cho " + attendance.fullName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return Routes.REDIRECT + Routes.Attendance.ROOT;
    }

    @PostMapping(Routes.Attendance.CHECK_OUT)
    public String checkOut(RedirectAttributes redirectAttributes) {
        try {
            AttendanceResponse attendance = attendanceService.checkOut();
            redirectAttributes.addFlashAttribute("successMessage", "Checkout thành công cho " + attendance.fullName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return Routes.REDIRECT + Routes.Attendance.ROOT;
    }

    @GetMapping(Routes.Attendance.HISTORY)
    public String attendanceHistory(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("attendanceHistory", attendanceService.getAttendanceHistory(pageable));
        return Views.Attendance.HISTORY;
    }

    @GetMapping(Routes.Attendance.LEAVE)
    public String leaveRequestList(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("leaveRequests", attendanceService.getAllLeaveRequests(pageable));
        return Views.Attendance.LEAVE_LIST;
    }

    @GetMapping(Routes.Attendance.LEAVE_CREATE)
    public String toLeaveRequestPage(Model model) {
        model.addAttribute("createLeaveRequest", new CreateLeaveRequest());
        return Views.Attendance.LEAVE_CREATE;
    }

    @PostMapping(Routes.Attendance.LEAVE_CREATE)
    public String createLeaveRequest(
            @Valid @ModelAttribute CreateLeaveRequest createLeaveRequest,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("createLeaveRequest", createLeaveRequest);
            return Views.Attendance.LEAVE_CREATE;
        }
        try {
            attendanceService.createLeaveRequest(createLeaveRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu nghỉ phép đã được gửi.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return Routes.REDIRECT + Routes.Attendance.ROOT + Routes.Attendance.LEAVE;
    }

}
