package com.attendenceSystem.module.attendance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.attendenceSystem.module.attendance.api")
public class AttendanceExceptionHandler {

    @ExceptionHandler({
        AlreadyCheckedInException.class,
        AlreadyCheckedOutException.class,
        NotCheckedInException.class,
        InvalidAttendanceStateException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedError(Exception ex) {
        log.error("Unexpected error in Attendance API", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Có lỗi xảy ra, vui lòng thử lại sau");
    }
}