package com.attendenceSystem.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Routes {
    public static final class Auth {
        public static final String ROOT = "/auth";
        public static final String LOGIN = "/login";
        public static final String REGISTER = "/register";
    }
    public static final class User {
        public static final String ROOT = "/user";
    }
}
