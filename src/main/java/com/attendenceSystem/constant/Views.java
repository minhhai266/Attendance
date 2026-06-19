package com.attendenceSystem.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Views {
    public static final class Auth {
        public static final String LOGIN = "cms/auth/login";
        public static final String REGISTER = "cms/auth/register";
    }

    public static final class User {
        public static final String LIST = "cms/account/account-list";
        public static final String DETAIL = "cms/account/account-detail";
        public static final String PROFILE = "cms/account/account-profile";
    }
        public static final class Dashboard {
        public static final String ADMIN = "cms/dashboard/dashboard-admin";
        public static final String MANAGER = "cms/dashboard/dashboard-manage";
    }
}
