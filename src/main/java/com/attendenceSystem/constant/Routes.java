package com.attendenceSystem.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Routes {
    public static final String REDIRECT = "/redirect:";

    public static final class Auth {
        public static final String ROOT = "/auth";
        public static final String LOGIN = "/login";
        public static final String REGISTER = "/register";
        public static final String FORGOT_PASSWORD = "/forgot_password";
        public static final String VERIFY_OTP = "verify-otp";
        public static final String CHANGE_PASSWORD = "/change_password";
    }

    public static final class User {
        public static final String ROOT = "/user";
        public static final String PROFILE = "/profile";
    }

    public static final class Dashboard {
        public static final String ROOT = "/dashboard";
        public static final String ADMIN = "/admin";
        public static final String MANAGER = "/manager";
        public static final String EMPLOYEE = "/employee";
    }
}
