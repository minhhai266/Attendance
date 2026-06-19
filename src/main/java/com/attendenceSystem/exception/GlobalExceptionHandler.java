// package com.attendenceSystem.exception;

// import org.springframework.http.HttpStatus;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.ResponseStatus;

// import com.attendenceSystem.exception.custom.BadRequestException;
// import com.attendenceSystem.exception.custom.ResourceNotFoundException;

// @ControllerAdvice
// public class GlobalExceptionHandler {
//     @ExceptionHandler(ResourceNotFoundException.class)
//     @ResponseStatus(HttpStatus.NOT_FOUND)
//     public String notFound() {
//         return "error/404";
//     }

//     @ExceptionHandler(AccessDeniedException.class)
//     @ResponseStatus(HttpStatus.FORBIDDEN)
//     public String forbidden() {
//         return "error/403";
//     }

//     @ExceptionHandler(BadRequestException.class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     public String badRequest() {
//         return "error/400";
//     }

//     @ExceptionHandler(Exception.class)
//     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//     public String internalServerError(Exception ex) {

//         ex.printStackTrace();

//         return "error/500";
//     }

// }