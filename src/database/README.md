# DIXY — Database Migrations

Flyway-style SQL migrations for the DIXY backend (`SIH2026-main`), covering
every table named in the roadmap: users, tenders, tender requirements,
bidders, bid documents, extracted entities, verification checks, compliance
results, risk/recommendations and audit logs.

## Files

| File | Table | Status vs. codebase |
|---|---|---|
| `V1__enable_extensions.sql` | — | infra |
| `V2__create_users_table.sql` | `users` | new (auth is currently in-memory `UserStorage`) |
| `V3__create_tenders_table.sql` | `tenders` | matches `Tender.java` |
| `V4__create_tender_requirements_table.sql` | `tender_requirements` | matches `TenderRequirement.java` |
| `V5__create_bidders_table.sql` | `bidders` | matches `Bidder.java` |
| `V6__create_tender_bids_table.sql` | `tender_bids` | matches `TenderBid.java` |
| `V7__create_documents_table.sql` | `documents` | matches `Document.java` |
| `V8__create_verification_results_table.sql` | `verification_results` | matches `VerificationResult.java` |
| `V9__create_extracted_entities_table.sql` | `extracted_entities` | new (normalized OCR fields) |
| `V10__create_compliance_results_table.sql` | `compliance_results` | new (Member 2's AI output) |
| `V11__create_recommendations_table.sql` | `recommendations` | new (AI recommendation + officer decision, kept separate on purpose) |
| `V12__create_audit_logs_table.sql` | `audit_logs` | new (Member 6) |
| `R__seed_demo_data.sql` | — | repeatable, dev/demo only — seeds Scenarios A, C, E from the roadmap |

`V3`–`V8` were written to match the existing `@Entity` classes **column for
column**, so `spring.jpa.hibernate.ddl-auto` can be switched from
`create-drop` to `validate` once these run, without Hibernate complaining
about a schema mismatch.

`V9`–`V12` are tables the roadmap calls for that don't have a JPA entity in
the repo yet (`extracted_entities`, `compliance_results`, `recommendations`,
`audit_logs`, `users`). They're additive — nothing in the existing code
touches them, so applying these migrations is safe even before the matching
Java entities exist. The VS Code prompt below covers adding those entities.

## Design notes

- **Foreign keys**: `tender_requirements` cascades from `tenders` (pure
  config). `tender_bids` uses `ON DELETE RESTRICT` against `tenders` and
  `bidders` — a bid is a business/audit record and should never vanish
  because a parent row was deleted. Everything hanging off a bid
  (`documents`, `verification_results`, `compliance_results`,
  `recommendations`, `extracted_entities`) cascades from `tender_bids`.
- **Enums as `CHECK` constraints**: every Java `enum` (`TenderStatus`,
  `BidStatus`, `DocumentType`, `DocumentStatus`, `Role`, …) gets a matching
  `CHECK` constraint so direct SQL from the Python OCR/Verification Hub
  services (or a bad migration) can't insert an invalid value.
- **`recommendations` keeps AI and human decisions separate**: `ai_recommendation`
  is written by the compliance engine; `officer_decision` starts at
  `PENDING` and is only set by a Procurement Officer. This mirrors the
  roadmap's core principle: *"AI must provide explanation/evidence rather
  than a black-box final decision... the final qualification/disqualification
  decision remains with the Procurement Officer."*
- **`audit_logs` denormalizes `actor_username`** so the log stays readable
  even if a `users` row is later deleted or renamed.
- **No unique constraint on `bidders.pan` / `bidders.gstin`** — duplicate
  identifiers across bidders are a fraud signal the AI pipeline needs to
  *detect* (Scenario D), not something the database should silently block.

## Applying these migrations

### Option 1 — plain psql (quick check, no Flyway yet)
```bash
for f in V1__enable_extensions.sql V2__create_users_table.sql \
         V3__create_tenders_table.sql V4__create_tender_requirements_table.sql \
         V5__create_bidders_table.sql V6__create_tender_bids_table.sql \
         V7__create_documents_table.sql V8__create_verification_results_table.sql \
         V9__create_extracted_entities_table.sql V10__create_compliance_results_table.sql \
         V11__create_recommendations_table.sql V12__create_audit_logs_table.sql \
         R__seed_demo_data.sql; do
  psql "$SPRING_DATASOURCE_URL" -U dixy -f "$f"
done
```

### Option 2 — Flyway (recommended, matches the roadmap's tech stack)
1. Copy every file in this folder into
   `src/main/resources/db/migration/` in the Gradle project.
2. Add the Flyway dependency and switch `ddl-auto` to `validate`
   (see `VS_CODE_PROMPT.md` for the exact prompt to hand an AI coding
   assistant to do this end-to-end, including new JPA entities for the
   tables that don't have one yet).
3. Run the app (or `./gradlew flywayMigrate`) — Flyway applies `V1`…`V12`
   in order, then `R__seed_demo_data.sql` whenever its contents change.

## Re-seeding demo data
`R__seed_demo_data.sql` truncates and reinserts its own tables, so it's safe
to re-run any time you need a clean demo state:
```bash
psql "$SPRING_DATASOURCE_URL" -U dixy -f R__seed_demo_data.sql
```
**Never point this at a production database** — it starts with `TRUNCATE ... CASCADE`.
