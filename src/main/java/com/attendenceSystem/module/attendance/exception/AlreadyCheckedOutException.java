package com.attendenceSystem.module.attendance.exception;

public class AlreadyCheckedOutException extends RuntimeException {
    public AlreadyCheckedOutException(String message) {
        super(message);
    }
}