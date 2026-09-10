-- DIXY | V9: extracted_entities  (roadmap item -- no JPA entity yet)
-- Normalized, queryable view of the key/value fields that already live as a
-- JSON blob in documents.ocr_extracted_fields. Member 2 (AI/Compliance) uses
-- this to run entity-matching / discrepancy queries (e.g. "compare PAN
-- across all documents for a bid") without parsing JSON in application code.
-- Populate it alongside documents.ocr_extracted_fields, not instead of it.

CREATE TABLE extracted_entities (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID          NOT NULL,
    field_name  VARCHAR(100)  NOT NULL,
    field_value TEXT,
    confidence  DOUBLE PRECISION,
    created_at  TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT fk_extracted_entities_document
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE
);

CREATE INDEX idx_extracted_entities_document_id ON extracted_entities (document_id);
CREATE INDEX idx_extracted_entities_field_name ON extracted_entities (field_name);
