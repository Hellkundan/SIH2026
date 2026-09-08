package com.dixy.backend.audit;

/**
 * Enumeration of key auditable actions across the DIXY Document Intelligence platform.
 * Supports events across authentication, tender lifecycle, bidder profiles, document processing,
 * compliance evaluations, and administrative oversight.
 */
public enum AuditAction {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    CREATE_TENDER,
    UPDATE_TENDER,
    DELETE_TENDER,
    CREATE_BIDDER,
    UPDATE_BIDDER,
    UPLOAD_DOCUMENT,
    DOWNLOAD_DOCUMENT,
    DELETE_DOCUMENT,
    RUN_VERIFICATION,
    COMPLIANCE_EVALUATED,
    FINAL_DECISION,
    ACCESS_DENIED,
    ADMIN_ACTION
}
