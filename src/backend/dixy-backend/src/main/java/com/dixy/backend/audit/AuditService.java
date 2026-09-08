package com.dixy.backend.audit;

import com.dixy.backend.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Map;

/**
 * Core service for recording and retrieving audit trails across DIXY.
 * Ensures consistent capturing of authenticated actor context, IP address,
 * and metadata sanitization to protect against credential leaks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Record an audit event using explicit actor and IP details (e.g. for authentication flows).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog recordEvent(
            String actor,
            Long actorId,
            AuditAction action,
            String entityType,
            String entityId,
            String ipAddress,
            AuditStatus status,
            String description,
            Map<String, Object> metadataMap
    ) {
        String metadataJson = null;
        if (metadataMap != null && !metadataMap.isEmpty()) {
            try {
                // Ensure sensitive keys are stripped safely on a mutable copy
                Map<String, Object> sanitized = new java.util.HashMap<>(metadataMap);
                sanitized.remove("password");
                sanitized.remove("token");
                sanitized.remove("secret");
                sanitized.remove("authorization");
                metadataJson = objectMapper.writeValueAsString(sanitized);
            } catch (Exception e) {
                log.warn("Failed to serialize audit metadata: {}", e.getMessage());
            }
        }

        AuditLog entry = AuditLog.builder()
                .actor(actor != null && !actor.isBlank() ? actor : "ANONYMOUS")
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress != null ? ipAddress : getClientIpAddress())
                .status(status)
                .description(description)
                .metadata(metadataJson)
                .timestamp(Instant.now())
                .build();

        try {
            return auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to persist audit log entry: {}", e.getMessage());
            return entry;
        }
    }

    /**
     * Reusable audit logging method that automatically resolves the current authenticated user and request context.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog record(
            AuditAction action,
            String entityType,
            String entityId,
            AuditStatus status,
            String description
    ) {
        return record(action, entityType, entityId, status, description, null);
    }

    /**
     * Reusable audit logging method with custom metadata map.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog record(
            AuditAction action,
            String entityType,
            String entityId,
            AuditStatus status,
            String description,
            Map<String, Object> metadataMap
    ) {
        String currentActor = "SYSTEM";
        Long currentActorId = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            currentActor = auth.getName();
            if (auth.getPrincipal() instanceof User user) {
                currentActorId = user.getId();
            }
        }

        return recordEvent(
                currentActor,
                currentActorId,
                action,
                entityType,
                entityId,
                getClientIpAddress(),
                status,
                description,
                metadataMap
        );
    }

    /**
     * Retrieve paginated audit events with strict upper bound protection.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(int page, int size) {
        int boundedSize = Math.min(Math.max(size, 1), 100);
        int boundedPage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Retrieve paginated audit logs for a specific entity.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByEntity(String entityType, String entityId, int page, int size) {
        int boundedSize = Math.min(Math.max(size, 1), 100);
        int boundedPage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    /**
     * Retrieve paginated audit logs for a specific actor username.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByActor(String actor, int page, int size) {
        int boundedSize = Math.min(Math.max(size, 1), 100);
        int boundedPage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByActor(actor, pageable);
    }

    /**
     * Retrieve a specific audit log by its ID.
     */
    @Transactional(readOnly = true)
    public AuditLog getAuditLogById(Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    /**
     * Helper to extract client remote IP from HttpServletRequest if within HTTP thread context.
     */
    private String getClientIpAddress() {
        try {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
