package com.attendenceSystem.module.otp.exception;

public class OtpNotFoundException extends RuntimeException {
    public OtpNotFoundException(String message) {
        super(message);
    }
}