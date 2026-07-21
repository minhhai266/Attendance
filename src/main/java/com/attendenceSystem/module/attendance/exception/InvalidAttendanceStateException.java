package com.attendenceSystem.module.attendance.exception;

public class InvalidAttendanceStateException extends RuntimeException {
    public InvalidAttendanceStateException(String message) {
        super(message);
    }
}