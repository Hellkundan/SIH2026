-- DIXY | R__seed_demo_data
-- Repeatable Flyway migration (re-runs whenever its checksum changes).
-- DEV/DEMO DATA ONLY -- do not run against a production database.
-- Wipes and reseeds a small, self-consistent dataset covering the SIH demo
-- scenarios from the roadmap (section 9):
--   A: fully compliant bidder
--   C: suspicious PAN/company mismatch
--   E: one verification provider unavailable, workflow still completes

BEGIN;

TRUNCATE TABLE
    audit_logs, recommendations, compliance_results, extracted_entities,
    verification_results, documents, tender_bids, tender_requirements,
    bidders, tenders, users
    RESTART IDENTITY CASCADE;

-- Users -----------------------------------------------------------------
INSERT INTO users (id, username, password_hash, role, full_name, email) VALUES
    ('11111111-1111-1111-1111-111111111111', 'officer.rao', '$2a$10$demoHashPlaceholderOfficer', 'PROCUREMENT_OFFICER', 'A. Rao', 'officer.rao@example.gov.in'),
    ('11111111-1111-1111-1111-111111111112', 'admin.sen', '$2a$10$demoHashPlaceholderAdmin', 'ADMIN', 'M. Sen', 'admin.sen@example.gov.in');

-- Tender ------------------------------------------------------------------
INSERT INTO tenders (id, title, description, status, created_at) VALUES
    ('22222222-2222-2222-2222-222222222221', 'Supply of Networking Equipment - GeM/2026/Q3',
     'Procurement of enterprise-grade networking hardware for district data centres.',
     'OPEN', now());

INSERT INTO tender_requirements (id, tender_id, requirement, mandatory) VALUES
    ('33333333-3333-3333-3333-333333333331', '22222222-2222-2222-2222-222222222221', 'Valid GSTIN registration', TRUE),
    ('33333333-3333-3333-3333-333333333332', '22222222-2222-2222-2222-222222222221', 'Valid PAN', TRUE),
    ('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222221', 'Udyam / MSME registration', FALSE),
    ('33333333-3333-3333-3333-333333333334', '22222222-2222-2222-2222-222222222221', 'EPFO compliance certificate', TRUE);

-- Bidders -------------------------------------------------------------------
INSERT INTO bidders (id, company_name, email, phone, pan, gstin, created_at) VALUES
    ('44444444-4444-4444-4444-444444444441', 'NorthStar Networks Pvt Ltd', 'contact@northstarnet.example', '9800000001', 'ABCDE1234F', '19ABCDE1234F1Z5', now()),
    ('44444444-4444-4444-4444-444444444442', 'Bluewave Infra Solutions', 'info@bluewaveinfra.example', '9800000002', 'PQRSX5678K', '27PQRSX5678K1Z2', now()),
    ('44444444-4444-4444-4444-444444444443', 'Sundial Systems LLP', 'hello@sundialsys.example', '9800000003', 'LMNOP9012Q', '07LMNOP9012Q1Z9', now());

-- Scenario A: NorthStar -- fully compliant bid -------------------------------
INSERT INTO tender_bids (id, tender_id, bidder_id, status, created_at, submitted_at) VALUES
    ('55555555-5555-5555-5555-555555555551', '22222222-2222-2222-2222-222222222221', '44444444-4444-4444-4444-444444444441',
     'PASSED_AUTOMATED_CHECKS', now(), now());

INSERT INTO documents (id, tender_bid_id, document_type, file_name, status, uploaded_at, document_hash, classification_confidence) VALUES
    ('66666666-6666-6666-6666-666666666661', '55555555-5555-5555-5555-555555555551', 'GST', 'northstar_gst_certificate.pdf', 'PROCESSED', now(), 'hash_ns_gst_001', 0.97),
    ('66666666-6666-6666-6666-666666666662', '55555555-5555-5555-5555-555555555551', 'PAN', 'northstar_pan_card.pdf', 'PROCESSED', now(), 'hash_ns_pan_001', 0.98);

INSERT INTO verification_results (id, tender_bid_id, verification_type, status, identifier, source, confidence, evidence, verified_at) VALUES
    ('77777777-7777-7777-7777-777777777771', '55555555-5555-5555-5555-555555555551', 'GST', 'VERIFIED', '19ABCDE1234F1Z5', 'GST_SANDBOX_PROVIDER', 0.96, 'GSTIN active, name matches company records.', now()),
    ('77777777-7777-7777-7777-777777777772', '55555555-5555-5555-5555-555555555551', 'PAN', 'VERIFIED', 'ABCDE1234F', 'PAN_SANDBOX_PROVIDER', 0.98, 'PAN valid and active.', now());

INSERT INTO compliance_results (id, tender_bid_id, entity_match_score, severity, discrepancies, explanation, evaluated_at) VALUES
    ('88888888-8888-8888-8888-888888888881', '55555555-5555-5555-5555-555555555551', 98.5, 'LOW', '[]', 'All extracted identifiers match across documents and government sources.', now());

INSERT INTO recommendations (id, tender_bid_id, compliance_result_id, ai_recommendation, ai_confidence, officer_decision, officer_id, officer_notes, decided_at) VALUES
    ('99999999-9999-9999-9999-999999999991', '55555555-5555-5555-5555-555555555551', '88888888-8888-8888-8888-888888888881', 'QUALIFICATION_RECOMMENDED', 0.97, 'QUALIFIED', '11111111-1111-1111-1111-111111111111', 'Reviewed and accepted AI recommendation.', now());

-- Scenario C: Bluewave -- suspicious PAN / company identity mismatch --------
INSERT INTO tender_bids (id, tender_id, bidder_id, status, created_at, submitted_at) VALUES
    ('55555555-5555-5555-5555-555555555552', '22222222-2222-2222-2222-222222222221', '44444444-4444-4444-4444-444444444442',
     'NEEDS_REVIEW', now(), now());

INSERT INTO documents (id, tender_bid_id, document_type, file_name, status, uploaded_at, document_hash, classification_confidence) VALUES
    ('66666666-6666-6666-6666-666666666663', '55555555-5555-5555-5555-555555555552', 'PAN', 'bluewave_pan_card.pdf', 'PROCESSED', now(), 'hash_bw_pan_001', 0.71);

INSERT INTO verification_results (id, tender_bid_id, verification_type, status, identifier, source, confidence, evidence, verified_at) VALUES
    ('77777777-7777-7777-7777-777777777773', '55555555-5555-5555-5555-555555555552', 'PAN', 'MISMATCH', 'PQRSX5678K', 'PAN_SANDBOX_PROVIDER', 0.55, 'PAN holder name does not match declared company name.', now());

INSERT INTO compliance_results (id, tender_bid_id, entity_match_score, severity, discrepancies, explanation, evaluated_at) VALUES
    ('88888888-8888-8888-8888-888888888882', '55555555-5555-5555-5555-555555555552', 42.0, 'CRITICAL',
     '[{"field":"company_identity","expected":"Bluewave Infra Solutions","found":"Bluewave Traders","severity":"CRITICAL"}]',
     'PAN-linked entity name conflicts with the declared bidder company name.', now());

INSERT INTO recommendations (id, tender_bid_id, compliance_result_id, ai_recommendation, ai_confidence, officer_decision) VALUES
    ('99999999-9999-9999-9999-999999999992', '55555555-5555-5555-5555-555555555552', '88888888-8888-8888-8888-888888888882', 'HIGH_RISK', 0.55, 'PENDING');

-- Scenario E: Sundial -- one verification provider unavailable --------------
INSERT INTO tender_bids (id, tender_id, bidder_id, status, created_at, submitted_at) VALUES
    ('55555555-5555-5555-5555-555555555553', '22222222-2222-2222-2222-222222222221', '44444444-4444-4444-4444-444444444443',
     'UNDER_REVIEW', now(), now());

INSERT INTO documents (id, tender_bid_id, document_type, file_name, status, uploaded_at, document_hash, classification_confidence) VALUES
    ('66666666-6666-6666-6666-666666666664', '55555555-5555-5555-5555-555555555553', 'EPFO', 'sundial_epfo_certificate.pdf', 'PROCESSED', now(), 'hash_sd_epfo_001', 0.88);

INSERT INTO verification_results (id, tender_bid_id, verification_type, status, identifier, source, error_state, confidence, evidence, verified_at) VALUES
    ('77777777-7777-7777-7777-777777777774', '55555555-5555-5555-5555-555555555553', 'EPFO', 'PENDING', NULL, 'EPFO_SANDBOX_PROVIDER', 'PROVIDER_TIMEOUT', NULL, 'Provider did not respond within the configured timeout; workflow continued.', now());

INSERT INTO recommendations (id, tender_bid_id, ai_recommendation, ai_confidence, officer_decision) VALUES
    ('99999999-9999-9999-9999-999999999993', '55555555-5555-5555-5555-555555555553', 'MANUAL_REVIEW', 0.40, 'PENDING');

-- Audit trail ---------------------------------------------------------------
INSERT INTO audit_logs (actor_user_id, actor_username, action, entity_type, entity_id, details) VALUES
    ('11111111-1111-1111-1111-111111111111', 'officer.rao', 'TENDER_CREATED', 'TENDER', '22222222-2222-2222-2222-222222222221', '{"title":"Supply of Networking Equipment - GeM/2026/Q3"}'),
    ('11111111-1111-1111-1111-111111111111', 'officer.rao', 'BID_QUALIFIED', 'TENDER_BID', '55555555-5555-5555-5555-555555555551', '{"reason":"All checks passed"}');

COMMIT;
