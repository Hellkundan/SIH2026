package com.dixy.backend.exception;

import com.dixy.backend.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception translation advice for all REST controllers.
 * Intercepts domain and framework exceptions and standardizes error responses
 * into the DIXY ApiResponse envelope while ensuring internal stack traces
 * are not leaked to external clients.
 */
@Slf4j
@RestControllerAdvice
@lombok.RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final com.dixy.backend.audit.AuditService auditService;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Failed authentication attempt from IP '{}' for path '{}'",
                request.getRemoteAddr(), request.getRequestURI());

        auditService.recordEvent(
                "ANONYMOUS",
                null,
                com.dixy.backend.audit.AuditAction.LOGIN_FAILURE,
                "Authentication",
                request.getRequestURI(),
                request.getRemoteAddr(),
                com.dixy.backend.audit.AuditStatus.FAILURE,
                "Authentication failed: Invalid username or password",
                null
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password", HttpStatus.UNAUTHORIZED.value(), request.getRequestURI()));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<?>> handleDisabledUser(
            DisabledException ex, HttpServletRequest request) {
        log.warn("Authentication attempt for disabled account from IP '{}'", request.getRemoteAddr());

        auditService.recordEvent(
                "DISABLED_USER",
                null,
                com.dixy.backend.audit.AuditAction.LOGIN_FAILURE,
                "Authentication",
                request.getRequestURI(),
                request.getRemoteAddr(),
                com.dixy.backend.audit.AuditStatus.FAILURE,
                "Authentication failed: Account disabled",
                null
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Account is disabled. Contact administrator.", HttpStatus.UNAUTHORIZED.value(), request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for path '{}' from IP '{}'",
                request.getRequestURI(), request.getRemoteAddr());

        auditService.record(
                com.dixy.backend.audit.AuditAction.ACCESS_DENIED,
                "Resource",
                request.getRequestURI(),
                com.dixy.backend.audit.AuditStatus.DENIED,
                "Access denied: Insufficient permissions for resource"
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You do not have permission to access this resource", HttpStatus.FORBIDDEN.value(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        log.debug("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Validation failed",
                        errors,
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.debug("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value(), request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        log.debug("Route not found: {}", ex.getResourcePath());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Endpoint not found: " + ex.getResourcePath(), HttpStatus.NOT_FOUND.value(), request.getRequestURI()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.debug("Duplicate resource conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value(), request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for path '{}': {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An internal error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI()));
    }
}
