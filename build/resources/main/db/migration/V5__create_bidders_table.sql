-- DIXY | V5: bidders
-- Mirrors backend.model.Bidder exactly. pan/gstin are intentionally NOT unique:
-- duplicate identifiers across bidders are a fraud signal the AI/verification
-- pipeline needs to detect (SIH Demo Scenario D), not something the DB should
-- silently block.

CREATE TABLE bidders (
    id           UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone        VARCHAR(30)  NOT NULL,
    pan          VARCHAR(20),
    gstin        VARCHAR(20),
    created_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_bidders_email ON bidders (email);
CREATE INDEX idx_bidders_pan ON bidders (pan);
CREATE INDEX idx_bidders_gstin ON bidders (gstin);
