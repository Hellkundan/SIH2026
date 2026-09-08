package com.dixy.backend.controller;

import com.dixy.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check and RBAC verification endpoints for the DIXY platform.
 */
@RestController
@Tag(name = "Health & RBAC", description = "System health and role-based access verification endpoints")
public class HealthController {

    /**
     * Public health probe for Docker, Kubernetes, and frontend status checks.
     */
    @GetMapping("/api/v1/public/health")
    @Operation(summary = "Service Health Check", description = "Public probe returning application status")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> health = Map.of(
            "status", "UP",
            "service", "DIXY Backend",
            "version", "0.0.1-SNAPSHOT"
        );
        return ResponseEntity.ok(ApiResponse.success("DIXY backend is healthy", health));
    }

    /**
     * Administration console endpoint, restricted to ROLE_ADMIN.
     */
    @GetMapping("/api/v1/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Admin Dashboard", description = "Restricted to users with ROLE_ADMIN authority")
    public ResponseEntity<ApiResponse<Map<String, String>>> adminDashboard() {
        Map<String, String> data = Map.of(
            "message", "Welcome to DIXY Admin Dashboard",
            "access", "FULL_SYSTEM_ADMIN"
        );
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard loaded", data));
    }

    /**
     * Procurement management endpoint, accessible to Procurement Officers and Admins.
     */
    @GetMapping("/api/v1/tender/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_OFFICER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tender List", description = "Restricted to PROCUREMENT_OFFICER and ADMIN roles")
    public ResponseEntity<ApiResponse<Map<String, String>>> listTenders() {
        Map<String, String> data = Map.of(
            "message", "Active tender listings retrieved",
            "access", "PROCUREMENT_OFFICER_VIEW"
        );
        return ResponseEntity.ok(ApiResponse.success("Tender list retrieved", data));
    }

    /**
     * Bidder status endpoint, accessible to authenticated Bidders, Officers, and Admins.
     */
    @GetMapping("/api/v1/bidder/my-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_OFFICER', 'BIDDER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Bidder Status", description = "Returns bidding and compliance submission status")
    public ResponseEntity<ApiResponse<Map<String, String>>> getBidderStatus(Authentication authentication) {
        Map<String, String> data = Map.of(
            "bidder", authentication.getName(),
            "status", "DOCUMENTS_PENDING"
        );
        return ResponseEntity.ok(ApiResponse.success("Bidder status retrieved", data));
    }
}
