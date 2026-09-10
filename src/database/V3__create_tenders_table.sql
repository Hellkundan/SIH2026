-- DIXY | V3: tenders
-- Mirrors backend.model.Tender exactly (id, title, description, status, createdAt)
-- so this migration is compatible with spring.jpa.hibernate.ddl-auto=validate.

CREATE TABLE tenders (
    id          UUID PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,

    CONSTRAINT chk_tenders_status CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED'))
);

CREATE INDEX idx_tenders_status ON tenders (status);
