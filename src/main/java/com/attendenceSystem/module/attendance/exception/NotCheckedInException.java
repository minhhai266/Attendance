package com.attendenceSystem.module.attendance.exception;

public class NotCheckedInException extends RuntimeException {
    public NotCheckedInException(String message) {
        super(message);
    }
}