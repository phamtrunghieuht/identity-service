package com.pthieu.identity_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.pthieu.identity_service.dto.request.ApiResponse;

import org.springframework.web.bind.MethodArgumentNotValidException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        // Log the exception (optional)
        System.err.println("Runtime Exception: " + ex.getMessage());
        
        // Return a user-friendly error message
        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode()); // Custom error code
        response.setStatus(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatus());
        response.setMessage(ex.getMessage() != null ? ex.getMessage() : ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        // Log the exception (optional)
        System.err.println("Runtime Exception: " + ex.getMessage());
        
        // Return a user-friendly error message
        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(errorCode.getCode()); // Custom error code
        response.setStatus(errorCode.getStatus());
        response.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        // Log the validation errors (optional)
        System.err.println("Validation Exception: " + ex.getMessage());
        
        // Return a user-friendly error message
        String enumKey = null;
        org.springframework.validation.FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null) {
            enumKey = fieldError.getDefaultMessage();
        }
        // Safely handle possible null value for enumKey
        if (enumKey == null) {
            enumKey = "Validation error";
        }
        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INVALID_KEY; // Fallback to a default error code
        }
        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setStatus(errorCode.getStatus());
        response.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

}
