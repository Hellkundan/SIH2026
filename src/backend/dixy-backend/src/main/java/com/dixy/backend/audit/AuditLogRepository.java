package com.dixy.backend.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Spring Data JPA Repository for querying audit log records with pagination and filters.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByActor(String actor, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(Instant start, Instant end, Pageable pageable);
}
