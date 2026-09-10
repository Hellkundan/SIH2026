-- DIXY | V4: tender_requirements
-- Mirrors backend.model.TenderRequirement exactly.
-- Cascades from tenders: requirements are pure configuration owned by the
-- tender and have no independent audit value once the tender is gone.

CREATE TABLE tender_requirements (
    id          UUID PRIMARY KEY,
    tender_id   UUID         NOT NULL,
    requirement VARCHAR(500) NOT NULL,
    mandatory   BOOLEAN      NOT NULL,

    CONSTRAINT fk_tender_requirements_tender
        FOREIGN KEY (tender_id) REFERENCES tenders (id) ON DELETE CASCADE
);

CREATE INDEX idx_tender_requirements_tender_id ON tender_requirements (tender_id);
