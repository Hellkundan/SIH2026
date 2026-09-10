-- DIXY | V12: audit_logs  (roadmap item -- no JPA entity yet)
-- "All important actions must be auditable" (roadmap section 5) and
-- Member 6 owns audit logging. actor_username is denormalized on purpose:
-- the log must stay readable/immutable even if the referenced user is later
-- deleted or renamed, so actor_user_id alone is not sufficient.
-- No UPDATE/DELETE is expected on this table in application code --
-- enforce that with DB permissions in production if possible.

CREATE TABLE audit_logs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id  UUID,
    actor_username VARCHAR(100),
    action         VARCHAR(100) NOT NULL,
    entity_type    VARCHAR(50)  NOT NULL,
    entity_id      UUID,
    details        TEXT,
    ip_address     VARCHAR(45),
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
