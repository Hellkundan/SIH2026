-- DIXY | V8: verification_results
-- Mirrors backend.model.VerificationResult exactly. One row per check per
-- provider (GST, Udyam, PAN, EPFO, ESIC, Startup India, NSIC, blacklist...)
-- as produced by Member 5's Verification Hub. evidence is a @Lob (JSON/text).

CREATE TABLE verification_results (
    id                 UUID PRIMARY KEY,
    tender_bid_id      UUID         NOT NULL,
    verification_type  VARCHAR(50)  NOT NULL,
    status             VARCHAR(30)  NOT NULL,
    identifier         VARCHAR(255),
    source             VARCHAR(100),
    error_state        VARCHAR(255),
    confidence         DOUBLE PRECISION,
    evidence           TEXT,
    verified_at        TIMESTAMP    NOT NULL,

    CONSTRAINT fk_verification_results_tender_bid
        FOREIGN KEY (tender_bid_id) REFERENCES tender_bids (id) ON DELETE CASCADE
);

CREATE INDEX idx_verification_results_tender_bid_id ON verification_results (tender_bid_id);
CREATE INDEX idx_verification_results_type ON verification_results (verification_type);
CREATE INDEX idx_verification_results_status ON verification_results (status);
