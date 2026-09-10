-- DIXY | V2: users
-- Backs Spring Security / JWT auth (Member 1 + Member 6).
-- Not yet a JPA entity in the codebase (auth currently uses the in-memory
-- backend.security.UserStorage) -- this table is forward-compatible with an
-- AppUser JPA entity so auth can move to the database without a schema change.

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    full_name     VARCHAR(150),
    email         VARCHAR(150),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('PROCUREMENT_OFFICER', 'ADMIN', 'BIDDER'))
);

CREATE INDEX idx_users_role ON users (role);

-- Keep updated_at current on every UPDATE.
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
