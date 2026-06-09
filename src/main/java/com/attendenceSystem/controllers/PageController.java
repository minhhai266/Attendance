package com.attendenceSystem.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class PageController {

    @GetMapping("home")
    public String home() {
        return "home";
    }
    @GetMapping("login")
    public String login() {
        return "auths/login";
    }
    @GetMapping("register")
    public String register() {
        return "auths/register";
    }
    @GetMapping("attendance")
    public String homeAttendance() {
        return "attendances/homeAttendance";
    }
    @GetMapping("dashboard")
    public String dashboard() {
        return "dashboard/employees/dashboard";
    }

    @GetMapping("test")
    @ResponseBody
    public String test() {
        return "OK";
    }
}
