package com.dixy.backend.audit;

import com.dixy.backend.dto.ApiResponse;
import com.dixy.backend.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing secure audit log queries for Administrators and Auditors.
 * Strictly restricted under RBAC rules to ROLE_ADMIN and ROLE_AUDITOR.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Trail", description = "Endpoints for regulatory audit log inspection and compliance verification")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Query Audit Trail", description = "Returns a paginated log of all system actions and security events")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLog> auditLogs = auditService.getAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit events retrieved successfully", auditLogs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Audit Event by ID", description = "Fetch a single audit log entry by its database identifier")
    public ResponseEntity<ApiResponse<AuditLog>> getAuditLogById(@PathVariable Long id) {
        AuditLog log = auditService.getAuditLogById(id);
        if (log == null) {
            throw new ResourceNotFoundException("Audit log entry not found with id: " + id);
        }
        return ResponseEntity.ok(ApiResponse.success("Audit event retrieved", log));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Query Audit Trail by Entity", description = "Returns all audit logs associated with a specific entity (e.g. Tender, Bidder, Document)")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLog> auditLogs = auditService.getAuditLogsByEntity(entityType, entityId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Entity audit logs retrieved successfully", auditLogs));
    }

    @GetMapping("/actor/{username}")
    @Operation(summary = "Query Audit Trail by Actor", description = "Returns all audit logs initiated by a given user")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogsByActor(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLog> auditLogs = auditService.getAuditLogsByActor(username, page, size);
        return ResponseEntity.ok(ApiResponse.success("Actor audit logs retrieved successfully", auditLogs));
    }
}
