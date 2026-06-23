package com.attendenceSystem.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Views {
    public static final class Auth {
        public static final String LOGIN = "cms/auth/login";
        public static final String REGISTER = "cms/auth/register";
        public static final String FORGOT_PASSWORD = "cms/password/forgot-password";
        public static final String VERIFY_OTP = "cms/auth/verify-otp";
        public static final String CHANGE_PASSWORD = "cms/password/change-password";
    }

    public static final class User {
        public static final String LIST = "cms/account/account-list";
        public static final String DETAIL = "cms/user/user-information";
        public static final String PROFILE = "cms/user/user-information";
    }

    public static final class Dashboard {
        public static final String ADMIN = "cms/dashboard/dashboard-admin";
        public static final String MANAGER = "cms/dashboard/dashboard-manage";
        public static final String EMPLOYEE = "cms/dashboard/dashboard-employee";
    }

    public static final class Attendance {
        public static final String ROOT = "cms/attendance/attendance";
        public static final String LIST = "cms/attendance/attendance-list";
        public static final String LEAVE_CREATE = "cms/absent/absent-create";
        public static final String LEAVE_LIST = "cms/absent/absent-list";
        public static final String LEAVE_HISTORY = "cms/absent/absent-history";
    }
}
