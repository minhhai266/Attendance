package com.attendenceSystem.frontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String homePage() {
        return "cms/homePage";
    }
    @GetMapping("/userInformation")
    public String userInformation() {
        return "cms/user/user-information";
    }
    @GetMapping("/reportDay")
    public String reportDay() {
        return "cms/report/report-day";
    }
    @GetMapping("/reportMonth")
    public String reportMonth() {
        return "cms/report/report-month";
    }
    @GetMapping("/faceIDCreate")
    public String faceIDCreate() {
        return "cms/face-id/faceID-create";
    }
    @GetMapping("/faceIDList")
    public String faceIDList() {
        return "cms/face-id/faceID-list";
    }
    @GetMapping("/documentCreate")
    public String documentCreate() {
        return "cms/document/document-create";
    }
    @GetMapping("/documentList")
    public String documentList() {
        return "cms/document/document-list";
    }
    @GetMapping("/dashboardAdmin")
    public String dashboardAdmin() {
        return "cms/dashboard/dashboard-admin";
    }
    @GetMapping("/dashboardEmployee")
    public String dashboardEmployee() {
        return "cms/dashboard/dashboard-employee";
    }
    @GetMapping("/dashboardManager")
    public String dashboardManager() {
        return "cms/dashboard/dashboard-manager";
    }

    @GetMapping("/login")
    public String login() {
        return "cms/auth/login";
    }
    @GetMapping("/register")
    public String register() {
        return "cms/auth/register";
    }
    @GetMapping("/verify-otp")
    public String verifyOTP() {
        return "cms/auth/verify-otp";
    }
    @GetMapping("/attendances")
    public String attendance() {
        return "cms/attendance/attendance";
    }
    @GetMapping("/attendanceCheck")
    public String attendancesCheck() {
        return "cms/attendance/attendance-check";
    }
    @GetMapping("/attendanceHistory")
    public String attendanceHistory() {
        return "cms/attendance/attendance-history";
    }

    @GetMapping("/attendanceLists")
    public String attendanceList() {
        return "cms/attendance/attendance-list";
    }
    @GetMapping("/accountList")
    public String accountList() {
        return "cms/account/account-list";
    }
    @GetMapping("/absentCreate")
    public String absentCreate() {
        return "cms/absent/absent-create";
    }
    @GetMapping("/absentHistory")
    public String absentHistory() {
        return "cms/absent/absent-history";
    }
    @GetMapping("/absentList")
    public String absentList() {
        return "cms/absent/absent-list";
    }
    @GetMapping("/changePassword")
    public String changePassword() {
        return "cms/password/change-password";
    }
    @GetMapping("/forgotPassword")
    public String forgotPassword() {
        return "cms/password/forgot-password";
    }
}