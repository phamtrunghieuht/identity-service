package com.pthieu.identity_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // Log the exception (optional)
        System.err.println("Runtime Exception: " + ex.getMessage());
        
        // Return a user-friendly error message
        return ResponseEntity.badRequest().body(null != ex.getMessage() ? ex.getMessage() : "An unexpected error occurred.");
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        // Log the validation errors (optional)
        System.err.println("Validation Exception: " + ex.getMessage());
        
        // Return a user-friendly error message
        return ResponseEntity.badRequest().body("Validation failed: " + ex.getFieldError().getDefaultMessage());
    }

}
