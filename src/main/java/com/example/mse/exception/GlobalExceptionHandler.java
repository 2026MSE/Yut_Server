package com.example.mse.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.mse.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {

        return ApiResponse.fail(e.getMessage(), "RUNTIME_ERROR");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {

        return ApiResponse.fail("Unexpected server error.", "INTERNAL_SERVER_ERROR");
    }
}