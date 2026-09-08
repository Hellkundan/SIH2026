package com.dixy.backend.audit;

/**
 * Result status of an individual audited action.
 * Note: Distinct from business verification status (e.g. VERIFIED/FLAGGED) -
 * this denotes the technical or security outcome of the user's action invocation.
 */
public enum AuditStatus {
    SUCCESS,
    FAILURE,
    DENIED
}
