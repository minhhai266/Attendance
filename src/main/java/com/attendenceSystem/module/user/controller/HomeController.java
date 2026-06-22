package com.attendenceSystem.module.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/homePage")
    public String homePage() {
        return "cms/homePage";
    }
    @GetMapping("/attendance")
    public String attendance() {
        return "cms/attendance/attendance";
    }
}