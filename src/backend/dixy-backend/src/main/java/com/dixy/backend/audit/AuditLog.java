package com.dixy.backend.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Immutable audit log record capturing all critical security and domain events.
 * Stored in PostgreSQL with indexed actor, action, entity, and timestamp fields
 * for fast forensic auditing and regulatory compliance.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_actor", columnList = "actor"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(name = "actor_id")
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditStatus status;

    @Column(length = 1000)
    private String description;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
