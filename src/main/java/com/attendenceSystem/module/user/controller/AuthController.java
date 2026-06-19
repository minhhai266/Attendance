package com.attendenceSystem.module.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.user.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
@RequestMapping(Routes.Auth.ROOT)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping(Routes.Auth.REGISTER)
    public String toRegisterPage() {
        return "auth/register";
    }
    
}
