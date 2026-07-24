package com.attendenceSystem.module.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RequiredArgsConstructor
@Controller
@RequestMapping(Routes.Schedule.ROOT)
public class ScheduleController {
    @GetMapping
    public String toSchedulePage(Model model) {
        return new String();
    }
    
}
