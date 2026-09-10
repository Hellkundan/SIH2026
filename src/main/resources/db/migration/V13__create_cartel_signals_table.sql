-- DIXY | V13: cartel_signals (roadmap section 12.5)
-- Stores collusion risk clusters detected by the AI Collusion Radar service.
-- One row per detected cluster per tender.

CREATE TABLE cartel_signals (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cluster_id          VARCHAR(100) NOT NULL,
    tender_id           UUID         NOT NULL,
    bidder_ids          TEXT         NOT NULL,
    connection_strength NUMERIC(5, 2),
    shared_signals      TEXT,
    pattern_flags       TEXT,
    explanation         TEXT,
    recommendation      VARCHAR(30)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_cartel_signals_tender
        FOREIGN KEY (tender_id) REFERENCES tenders (id) ON DELETE CASCADE,

    CONSTRAINT chk_cartel_signals_recommendation CHECK (
        recommendation IN ('NO_ACTION', 'FLAG_FOR_REVIEW', 'HIGH_RISK_CARTEL')
    )
);

CREATE INDEX idx_cartel_signals_tender_id ON cartel_signals (tender_id);
