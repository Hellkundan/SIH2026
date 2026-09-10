-- DIXY | V1: Core extensions
-- gen_random_uuid() is used by seed/demo data and any service that inserts
-- rows directly in SQL (e.g. Python OCR / Verification Hub microservices).
-- The Spring Boot app itself generates UUIDs in Java (GenerationType.UUID),
-- so this is a safety net, not a requirement for JPA writes.

CREATE EXTENSION IF NOT EXISTS pgcrypto;
