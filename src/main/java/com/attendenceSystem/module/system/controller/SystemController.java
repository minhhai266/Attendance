package com.attendenceSystem.module.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.constant.Views;

@Controller
@RequestMapping(Routes.System.ROOT)
public class SystemController {

    @GetMapping(Routes.System.SETTING)
    public String setting() {
        return Views.System.SETTING;
    }
}