package com.pthieu.identity_service.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Error", "An uncategorized error occurred"),
    INVALID_REQUEST(400, "Error", "Invalid request"),
    INVALID_KEY(4001, "Error", "Invalid key"),
    INVALID_TOKEN(4001, "Error", "Invalid token"),
    UNAUTHORIZED(401, "Error", "Unauthorized"),
    UNAUTHENTICATED(401, "Error", "Unauthenticated"),
    NOT_FOUND(404, "Error", "Not found"),

    USER_EXISTS(1001, "Error", "User already exists"),
    USER_NOT_FOUND(1002, "Error", "User not found"),
    USERNAME_INVALID(1003, "Error", "Username must be at least 3 characters long"),
    PASSWORD_INVALID(1003, "Error", "Password must be at least 8 characters long"),
    USER_NOT_EXISTS(1004, "Error", "User does not exist");


    private final int code;
    private final String status;
    private final String message;

    ErrorCode(int code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
