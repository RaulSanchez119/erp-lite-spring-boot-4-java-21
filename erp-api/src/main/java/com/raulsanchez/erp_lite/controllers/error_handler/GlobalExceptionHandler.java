package com.raulsanchez.erp_lite.controllers.error_handler;

import com.raulsanchez.erp_lite.domain.exceptions.MyBusinessException;
import com.raulsanchez.erp_lite.dtos.ErrorResponse;
import com.raulsanchez.erp_lite.exceptions.CommandException;
import com.raulsanchez.erp_lite.exceptions.QueryException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /*
     * HTTP STATUS 409
     */
    @ExceptionHandler(MyBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            MyBusinessException ex, HttpServletRequest request) {

        log.warn("Rule MyBusinessException violation detected");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT,
                        "Business rule violation",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /*
     * HTTP STATUS 422
     */
    @ExceptionHandler(CommandException.class)
    public ResponseEntity<ErrorResponse> handleCommandException(
            CommandException ex, HttpServletRequest request) {

        log.warn("Rule CommandException violation detected");

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ErrorResponse.of(
                        HttpStatus.UNPROCESSABLE_CONTENT,
                        "Command rule violation",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /*
     * HTTP STATUS 404 - 500
     */
    @ExceptionHandler(QueryException.class)
    public ResponseEntity<ErrorResponse> handleQueryException(
            QueryException ex, HttpServletRequest request) {

        log.warn("Rule QueryException violation detected");

        boolean isInfraFailure = ex.getCause() instanceof RuntimeException;

        if (isInfraFailure) {

            log.error("Rule QueryException by infrastructure detected");
            return get500Response(request.getRequestURI());

        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(
                            HttpStatus.NOT_FOUND,
                            "Resource wasn't found",
                            ex.getMessage(),
                            request.getRequestURI()
                    ));
        }

    }

    /*
     * HTTP STATUS 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handlerRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("General error detected");

        return get500Response(request.getRequestURI());
    }

    /*
     * HTTP STATUS 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handlerMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ?
                                fieldError.getDefaultMessage() : "InvalidValue",
                        (existingValue, newValue) -> newValue
                ));

        return ResponseEntity.badRequest().body(errors);
    }

    private ResponseEntity<ErrorResponse> get500Response(String uri) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal error occurred",
                        "Unexpected error occurred",
                        uri
                ));
    }
}