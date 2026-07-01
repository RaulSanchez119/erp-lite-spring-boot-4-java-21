package com.raulsanchez.erp_lite.dtos;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {

    public static ErrorResponse of(HttpStatus httpStatus, String error, String message, String path) {
        return new ErrorResponse(
                Instant.now(),
                httpStatus.value(),
                error,
                message,
                path
        );
    }
}