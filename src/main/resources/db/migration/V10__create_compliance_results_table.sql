-- DIXY | V10: compliance_results  (roadmap item -- no JPA entity yet)
-- One row per bid: the aggregated output of Member 2's AI/compliance engine
-- (entity match score, discrepancies, severity, explanation). This is the
-- evidence layer the Risk & Recommendation Engine and Officer Dashboard read
-- from. AI never writes a final decision here -- see recommendations.officer_decision.

CREATE TABLE compliance_results (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tender_bid_id      UUID           NOT NULL,
    entity_match_score NUMERIC(5, 2),
    severity           VARCHAR(20),
    discrepancies      TEXT,
    explanation        TEXT,
    evaluated_at       TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT fk_compliance_results_tender_bid
        FOREIGN KEY (tender_bid_id) REFERENCES tender_bids (id) ON DELETE CASCADE,
    CONSTRAINT chk_compliance_results_severity CHECK (
        severity IS NULL OR severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
    ),
    CONSTRAINT chk_compliance_results_score CHECK (
        entity_match_score IS NULL OR (entity_match_score BETWEEN 0 AND 100)
    )
);

CREATE INDEX idx_compliance_results_tender_bid_id ON compliance_results (tender_bid_id);
CREATE INDEX idx_compliance_results_severity ON compliance_results (severity);
