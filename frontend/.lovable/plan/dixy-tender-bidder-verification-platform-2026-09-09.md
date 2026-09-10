# DIXY — Tender & Bidder Verification Platform

A 16-page GovTech web app with two sides: bidders who upload compliance documents and bid on tenders, and procurement officers who publish tenders and review bids. Everything runs on realistic sample data now, behind a single swap point so your existing Spring Boot API can take over later. Sign-in talks to your real `/auth/login`.

## Look and feel

Navy blue and white with a teal accent, generous whitespace, modern sans-serif. Consistent status colours everywhere: green verified/qualified, amber pending or manual review, red failed/disqualified, grey draft. Data-dense tables on the officer side, calmer cards on the bidder side. Every list has an empty state, a loading state and an error state; uploads and verification runs show toasts.

## Pages

Public
1. Landing — hero, stats strip, four feature cards, three-step teaser, footer
2. How It Works — bidder journey, officer journey, verification pipeline diagram
3. Login — email/password, role detected from the response, error state, forgot-password stub
4. Register — company name, email, phone, password, optional business type and address
5. Not-found / error page

Bidder
6. Dashboard — verification score badge, quick actions, 9-check compliance checklist, activity feed, active bids table
7. Browse Tenders — search, filters, deadline countdown, eligibility hint
8. Tender Detail — full tender, requirements panel, "you meet 4/5" widget, Start Bid (disabled until core docs verified)
9. Document Upload & Verification Center — drag-and-drop per document type, status timeline, OCR panel with extracted fields and confidence, registry-check panel, re-run verification
10. Bid Submission Wizard — requirements check, attach documents, bid details, review and submit, save as draft at every step, confirmation with reference ID
11. My Bids — status table plus per-bid detail with documents, verification snapshot, officer remarks and status history

Officer
12. Dashboard — KPI cards, bids-by-status and tenders-by-status charts, bids awaiting review, tenders closing soon
13. Create / Manage Tender — list with filters, create/edit form, Open / Close / Edit / View Bids actions
14. Requirements Builder — toggle mandatory document types, add numeric/text criteria, live bidder-side preview
15. Bid Review & Compliance — bid list per tender, side-by-side OCR and registry results, compliance score, blacklist flag, document viewer modal, Review / Qualify / Disqualify with remarks
16. Bidder Verification Report — full profile, audit trail across all 9 checks with timestamps and provider responses, blacklist banner, printable

Shared: role-aware top nav with notifications bell and profile menu, dashboard sidebar, sign-in guard that sends unauthenticated visitors to Login.

## Technical notes

- One data layer (`src/lib/api/*`) exposes typed functions matching your contract: `/tenders`, `/tender-requirements`, `/tender-bids`, `/bidders`, `/documents` plus their action endpoints. Today each returns seeded fixtures; a single `USE_MOCK` flag flips them to real HTTP calls, so no page component changes when you swap.
- Types mirror your models exactly: Tender, Bidder, Document, TenderBid, TenderRequirement, and the DocumentType / TenderStatus / BidStatus / VerificationStatus / error-state enums.
- Login posts to your `/auth/login` through a small server-side proxy so browser CORS is never an issue; the returned JWT is kept client-side and attached as a bearer token to future calls. I need the base URL of your Spring Boot API — until you give it, login also accepts two demo accounts (bidder and officer) so all 16 pages are reviewable.
- Routes follow the app's file-based routing, with the bidder and officer portals behind the auth guard and all public pages carrying their own page titles and social preview text.
- Charts use the chart library already available in the project.

## Out of scope for this pass

Real OCR execution, live registry calls, PDF export (the report page prints via the browser), and real-time notifications.
