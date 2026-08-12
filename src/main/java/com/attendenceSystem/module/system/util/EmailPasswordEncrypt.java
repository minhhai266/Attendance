package com.attendenceSystem.module.system.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailPasswordEncrypt {
    public String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        return "*".repeat(password.length());
    }
}
