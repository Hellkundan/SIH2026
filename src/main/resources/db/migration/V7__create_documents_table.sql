-- DIXY | V7: documents
-- Mirrors backend.model.Document exactly, including the two @Lob text
-- fields (extracted_text, ocr_extracted_fields) which store raw OCR text
-- and a JSON blob of structured fields respectively (Member 3's output).
-- document_hash powers duplicate-document detection (Scenario D).

CREATE TABLE documents (
    id                        UUID PRIMARY KEY,
    tender_bid_id             UUID          NOT NULL,
    document_type             VARCHAR(30)   NOT NULL,
    file_name                 VARCHAR(255)  NOT NULL,
    status                    VARCHAR(20)   NOT NULL,
    uploaded_at                TIMESTAMP     NOT NULL,
    file_path                 VARCHAR(500),
    extracted_text            TEXT,
    document_hash             VARCHAR(128),
    classification_confidence DOUBLE PRECISION,
    ocr_extracted_fields      TEXT,

    CONSTRAINT fk_documents_tender_bid
        FOREIGN KEY (tender_bid_id) REFERENCES tender_bids (id) ON DELETE CASCADE,
    CONSTRAINT chk_documents_type CHECK (document_type IN (
        'PAN', 'GST', 'UDYAM', 'EPFO', 'ESIC', 'STARTUP_INDIA',
        'NSIC', 'OEM_AUTHORIZATION', 'MAKE_IN_INDIA', 'OTHER'
    )),
    CONSTRAINT chk_documents_status CHECK (status IN (
        'UPLOADED', 'PROCESSING', 'PROCESSED', 'FAILED'
    ))
);

CREATE INDEX idx_documents_tender_bid_id ON documents (tender_bid_id);
CREATE INDEX idx_documents_type ON documents (document_type);
CREATE INDEX idx_documents_hash ON documents (document_hash);
