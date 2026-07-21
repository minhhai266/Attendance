package com.attendenceSystem.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Routes {
    public static final String REDIRECT = "redirect:";
    public static final String API = "/api";

    public static final class Action {
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update";
        public static final String DELETE = "/delete";
        public static final String DEACTIVATE = "/deactivate";
        public static final String ACTIVATE = "/activate";
    }

    public static final class Auth {
        public static final String ROOT = "/auth";
        public static final String LOGIN = "/login";
        public static final String REGISTER = "/register";
        public static final String FORGOT_PASSWORD = "/forgot-password";
        public static final String VERIFY_OTP = "/verify-otp";
        public static final String CHANGE_PASSWORD = "/change-password";
        public static final String UPDATE_PASSWORD = "/update-password";
        public static final String LOGOUT = "/logout";
    }

    public static final class User {
        public static final String ROOT = "/user";
        public static final String PROFILE = "/profile";
        public static final String CHANGE_PASSWORD = "/change-password";
        public static final String UPDATE_PASSWORD = "/update-password";
        public static final String UPDATE_INFORMATION = "/update-information";
    }

    public static final class Account {
        public static final String ROOT = "/admin/accounts";
        public static final String MANAGER = "/manager/accounts";
        public static final String CREATE = "/create";
        public static final String UPDATE = "/update";
        public static final String DELETE = "/delete";
        public static final String DEACTIVATE = "/deactivate";
        public static final String ACTIVATE = "/activate";
        public static final String UPDATE_INFORMATION = "/update-information";
    }

    public static final class Dashboard {
        public static final String ROOT = "/dashboard";
        public static final String ADMIN = "/admin";
        public static final String MANAGER = "/manager";
        public static final String EMPLOYEE = "/employee";
    }
    public static final class Attendance {
        public static final String ROOT = "/attendance";
        public static final String HISTORY = "/history";
        public static final String LEAVE = "/leave";
        public static final String CHECK_IN = "/check-in";
        public static final String CHECK_OUT = "/check-out";
    }
    
    public static final class FaceId {
        public static final String ROOT = "/face-id";
    }

    public static final class Schedule {
        public static final String ROOT = "/schedule";
    }
}