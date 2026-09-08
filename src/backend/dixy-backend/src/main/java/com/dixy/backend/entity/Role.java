package com.dixy.backend.entity;

/**
 * DIXY Platform Roles
 * ────────────────────
 * Corresponds to key stakeholders in Government Procurement / GeM compliance:
 * - ROLE_ADMIN: System Administrator with full access
 * - ROLE_PROCUREMENT_OFFICER: Tender publishing, bid evaluation, compliance reporting
 * - ROLE_BIDDER: Vendor / Supplier submitting bids and viewing compliance feedback
 * - ROLE_AUDITOR: Read-only access to audit logs, compliance reports, and integrity checks
 */
public enum Role {
    ROLE_ADMIN,
    ROLE_PROCUREMENT_OFFICER,
    ROLE_BIDDER,
    ROLE_AUDITOR
}
