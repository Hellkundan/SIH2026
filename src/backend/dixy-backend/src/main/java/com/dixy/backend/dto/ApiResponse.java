package com.dixy.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * DIXY Standard API Response Wrapper — Phase 9 (used from Phase 3 onwards)
 * ─────────────────────────────────────────────────────────────────────────
 * EVERY API response from DIXY should use this wrapper.
 * This gives the team a consistent contract so Member 4 (Frontend)
 * always knows what format to expect.
 *
 * Example success response:
 * {
 *   "success": true,
 *   "message": "Login successful",
 *   "data": { "token": "eyJ..." },
 *   "timestamp": "2026-09-07T00:00:00Z",
 *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
 * }
 *
 * Example error response:
 * {
 *   "success": false,
 *   "message": "Invalid credentials",
 *   "data": null,
 *   "timestamp": "2026-09-07T00:00:00Z",
 *   "requestId": "550e8400-e29b-41d4-a716-446655440001"
 * }
 *
 * JAVA CONCEPT — Generics <T>:
 *   ApiResponse<String>  — data is a String
 *   ApiResponse<UserDTO> — data is a UserDTO object
 *   ApiResponse<List<TenderDTO>> — data is a list
 *   This lets one class handle all response types.
 *
 * LOMBOK @Builder lets us write:
 *   ApiResponse.success("Done", myData)
 * instead of 10 lines of constructor code.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // Don't include null fields in JSON
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer status;
    private String timestamp;
    private String path;
    private String requestId;

    /**
     * Creates a successful response with data.
     * Usage: return ResponseEntity.ok(ApiResponse.success("Done", myData));
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(Instant.now().toString())
            .requestId(UUID.randomUUID().toString())
            .build();
    }

    /**
     * Creates a successful response without data payload.
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .timestamp(Instant.now().toString())
            .requestId(UUID.randomUUID().toString())
            .build();
    }

    /**
     * Creates an error response without explicit status and path (legacy helper).
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .timestamp(Instant.now().toString())
            .requestId(UUID.randomUUID().toString())
            .build();
    }

    /**
     * Creates a standardized error response with HTTP status code and request URI path.
     */
    public static <T> ApiResponse<T> error(String message, int status, String path) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .status(status)
            .path(path)
            .timestamp(Instant.now().toString())
            .requestId(UUID.randomUUID().toString())
            .build();
    }

    /**
     * Creates a standardized error response with data payload, HTTP status code, and request URI path.
     */
    public static <T> ApiResponse<T> error(String message, T data, int status, String path) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .data(data)
            .status(status)
            .path(path)
            .timestamp(Instant.now().toString())
            .requestId(UUID.randomUUID().toString())
            .build();
    }
}
