package com.tonilr.ToDoList.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Standardized error response class for API error handling.
 * Provides a consistent structure for all error responses including
 * status codes, error messages, timestamps, and additional details.
 */
public class ErrorResponse {
    private int status;
    private int errorCode;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private String path;
    private List<String> errors;

    /**
     * Constructor for creating error responses with full details.
     * @param status HTTP status code
     * @param errorCode Application-specific error code
     * @param details Detailed error description
     * @param path Request path that caused the error
     */
    public ErrorResponse(int status, ErrorCode errorCode, String details, String path) {
        this.status = status;
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.details = details;
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.errors = new ArrayList<>();
    }

    /**
     * Constructor for creating simple error responses.
     * @param message Error message
     */
    public ErrorResponse(String message) {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    /**
     * Adds an additional error message to the error list.
     * @param error Error message to add
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
