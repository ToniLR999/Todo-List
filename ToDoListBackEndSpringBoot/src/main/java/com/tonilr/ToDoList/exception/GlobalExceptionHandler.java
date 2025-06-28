package com.tonilr.ToDoList.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Provides centralized error handling for all controllers,
 * converting exceptions into standardized ErrorResponse objects
 * with appropriate HTTP status codes and detailed error information.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns 404 status.
     * @param ex The ResourceNotFoundException that was thrown
     * @param request The web request that caused the exception
     * @return ResponseEntity with ErrorResponse and 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ErrorCode.RESOURCE_NOT_FOUND,
            ex.getMessage(),
            ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles BadRequestException and returns 400 status.
     * @param ex The BadRequestException that was thrown
     * @param request The web request that caused the exception
     * @return ResponseEntity with ErrorResponse and 400 status
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ErrorCode.INVALID_INPUT,
            ex.getMessage(),
            ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles UnauthorizedException and returns 401 status.
     * @param ex The UnauthorizedException that was thrown
     * @param request The web request that caused the exception
     * @return ResponseEntity with ErrorResponse and 401 status
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ErrorCode.UNAUTHORIZED_ACCESS,
            ex.getMessage(),
            ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles validation exceptions from @Valid annotations.
     * Extracts field-specific validation errors and returns them in a structured format.
     * @param ex The MethodArgumentNotValidException that was thrown
     * @param request The web request that caused the exception
     * @return ResponseEntity with ErrorResponse containing validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ErrorCode.VALIDATION_ERROR,
            "Error de validación en los datos de entrada",
            ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        error.setDetails(errors.toString());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other unhandled exceptions and returns 500 status.
     * Provides a generic error message to avoid exposing internal details.
     * @param ex The Exception that was thrown
     * @param request The web request that caused the exception
     * @return ResponseEntity with ErrorResponse and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ErrorCode.INTERNAL_ERROR,
            "Ha ocurrido un error inesperado",
            ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
