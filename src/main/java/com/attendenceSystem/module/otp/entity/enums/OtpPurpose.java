package com.attendenceSystem.module.otp.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OtpPurpose {
    FORGOT_PASSWORD(1, "Reset Password"),
    REGISTER(2, "Register Account"),
    CHANGE_EMAIL(3, "Change Email");

    private final int value;
    private final String name;

    public static OtpPurpose fromValue(int value) {
        for (OtpPurpose p : OtpPurpose.values()) {
            if (p.value == value) {
                return p;
            }
        }
        return null;
    }
    public static OtpPurpose fromName(String name) {
        for (OtpPurpose p : OtpPurpose.values()) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        return null;
    }
}