-- DIXY | V6: tender_bids
-- Mirrors backend.model.TenderBid exactly.
-- RESTRICT (not CASCADE) on tender_id/bidder_id: a bid is a business/audit
-- record and must never disappear silently because a tender or bidder row
-- was deleted. Use status transitions (see BidStatus) or a soft-delete
-- column in production instead of hard deletes.

CREATE TABLE tender_bids (
    id           UUID PRIMARY KEY,
    tender_id    UUID        NOT NULL,
    bidder_id    UUID        NOT NULL,
    status       VARCHAR(30) NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    submitted_at TIMESTAMP,

    CONSTRAINT fk_tender_bids_tender
        FOREIGN KEY (tender_id) REFERENCES tenders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_tender_bids_bidder
        FOREIGN KEY (bidder_id) REFERENCES bidders (id) ON DELETE RESTRICT,
    CONSTRAINT chk_tender_bids_status CHECK (status IN (
        'DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'QUALIFIED',
        'DISQUALIFIED', 'NEEDS_REVIEW', 'PASSED_AUTOMATED_CHECKS'
    ))
);

CREATE INDEX idx_tender_bids_tender_id ON tender_bids (tender_id);
CREATE INDEX idx_tender_bids_bidder_id ON tender_bids (bidder_id);
CREATE INDEX idx_tender_bids_status ON tender_bids (status);
