-- DIXY | V11: recommendations  (roadmap item -- no JPA entity yet)
-- Keeps the AI's recommendation and the Procurement Officer's final decision
-- as two clearly separate fields on purpose: "AI must provide
-- explanation/evidence rather than a black-box final decision" and
-- "the final qualification/disqualification decision remains with the
-- Procurement Officer" (roadmap sections 1 and 5).

CREATE TABLE recommendations (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tender_bid_id         UUID          NOT NULL,
    compliance_result_id  UUID,
    ai_recommendation     VARCHAR(40),
    ai_confidence         NUMERIC(5, 2),
    officer_decision      VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    officer_id            UUID,
    officer_notes         TEXT,
    decided_at            TIMESTAMP,
    created_at            TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT fk_recommendations_tender_bid
        FOREIGN KEY (tender_bid_id) REFERENCES tender_bids (id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendations_compliance_result
        FOREIGN KEY (compliance_result_id) REFERENCES compliance_results (id) ON DELETE SET NULL,
    CONSTRAINT fk_recommendations_officer
        FOREIGN KEY (officer_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_recommendations_ai_rec CHECK (
        ai_recommendation IS NULL OR ai_recommendation IN (
            'QUALIFICATION_RECOMMENDED', 'MANUAL_REVIEW', 'DOCUMENT_REQUIRED', 'HIGH_RISK'
        )
    ),
    CONSTRAINT chk_recommendations_officer_decision CHECK (
        officer_decision IN ('PENDING', 'QUALIFIED', 'DISQUALIFIED', 'NEEDS_MORE_INFO')
    )
);

CREATE INDEX idx_recommendations_tender_bid_id ON recommendations (tender_bid_id);
CREATE INDEX idx_recommendations_officer_decision ON recommendations (officer_decision);
