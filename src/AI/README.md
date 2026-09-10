# DIXY AI Engine

## DIXY-AI-v0.2

AI-powered bidder identity consistency and compliance intelligence
engine for the DIXY procurement platform.

> **AI ASSISTS → HUMAN DECIDES**

DIXY AI is a decision-support system. It analyzes structured evidence
extracted from documents and supplied verification results, highlights
inconsistencies and anomalies, and recommends what the Procurement
Officer should review.

It does **not** automatically approve, reject, disqualify, or declare a
bidder fraudulent.

---

## Table of Contents

1. [What is DIXY AI?](#1-what-is-dixy-ai)
2. [High-Level Architecture](#2-high-level-architecture)
3. [One-Bidder Identity Intelligence](#3-one-bidder-identity-intelligence)
4. [Identity Graph](#4-identity-graph)
5. [Missing Evidence vs Conflict](#5-missing-evidence-vs-conflict)
6. [OCR / Document Intelligence Boundary](#6-ocr--document-intelligence-boundary)
7. [Dynamic Identity Extraction](#7-dynamic-identity-extraction)
8. [Deterministic Checks vs AI/ML](#8-deterministic-checks-vs-aiml)
9. [Entity Resolution Model](#9-entity-resolution-model)
10. [Cross-Document Consistency](#10-cross-document-consistency)
11. [Identifier Checks](#11-identifier-checks)
12. [Verification Module](#12-verification-module)
13. [Anomaly Detection](#13-anomaly-detection)
14. [Risk Scoring](#14-risk-scoring)
15. [Evidence Coverage](#15-evidence-coverage)
16. [Master AI Function](#16-master-ai-function)
17. [Current AI Output](#17-current-ai-output)
18. [Human-Readable Example](#18-human-readable-example)
19. [Example Identity Conflict](#19-example-identity-conflict)
20. [Backend Integration](#20-backend-integration)
21. [FastAPI Service](#21-fastapi-service)
22. [Backend Integration Flow](#22-backend-integration-flow)
23. [Team Responsibilities](#23-team-responsibilities)
24. [Frontend Display Recommendation](#24-frontend-display-recommendation)
25. [Repository Structure](#25-repository-structure)
26. [Model Artifact Usage](#26-model-artifact-usage)
27. [Current Prototype Status](#27-current-prototype-status)
28. [Important Prototype Limitations](#28-important-prototype-limitations)
29. [Identity vs Verification vs Compliance vs Decision](#29-identity-vs-verification-vs-compliance-vs-decision)
30. [Quick Integration Summary](#30-quick-integration-summary)
31. [DIXY Design Principle](#31-dixy-design-principle)

---

## Quick Start for Developers

**Backend Integration:**

```bash
# Start DIXY AI service (if not already running)
python dixy_api.py
```

**API Endpoint:**

```bash
curl -X POST http://localhost:8000/api/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{"bidder_data": {...}, "verification_results": {...}}'
```

**Health Check:**

```bash
curl http://localhost:8000/health
```

---

## 1. What is DIXY AI? {#1-what-is-dixy-ai}

DIXY AI is the intelligence layer of the DIXY procurement platform.

The AI service receives structured information belonging to **one
bidder**. It does not primarily compare one bidder against another
bidder.

Its current responsibilities include:

- Cross-document identity consistency
- Company/name similarity
- Identifier relationship checking
- PAN ↔ GSTIN consistency
- PAN ↔ Udyam consistency
- PAN ↔ ITR consistency
- GSTIN ↔ Udyam consistency
- MCA/company identity comparison
- State consistency checks
- Identifier format validation
- Identity Graph analysis
- Anomaly detection
- Risk scoring
- Evidence coverage
- Verification-result analysis
- Explainable recommendations

The system is designed to answer:

> **Do the submitted pieces of evidence appear to belong to the same
> underlying bidder, and are there conflicts that require human
> review?**

---

# 2. High-Level Architecture {#2-high-level-architecture}

```text
                    BIDDER DOCUMENTS
                           |
                           v
                  +------------------+
                  | OCR / Extraction |
                  |     Module       |
                  +--------+---------+
                           |
                           | Structured JSON
                           v
                  +------------------+
                  |     BACKEND      |
                  | Java / Kotlin    |
                  +--------+---------+
                           |
                           | HTTP POST
                           v
                  +------------------+
                  |    DIXY AI       |
                  |     FastAPI      |
                  +--------+---------+
                           |
                           v
                  +------------------+
                  |   dixy_analyze() |
                  +--------+---------+
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
   Consistency       Identifier       Identity Graph
      Engine            Checks            Engine
          |                |                |
          +----------------+----------------+
                           |
                           v
                    Risk Analysis
                    & Anomaly Detection
                           |
                           v
                Verification Analysis
                           |
                           v
                Explainable Recommendation
                           |
                           v
          Return Analysis JSON to Backend

          +----------------+----------------+
                           |
                           v
                    Risk + Anomaly
                           |
                           v
                Verification Analysis
                           |
                           v
                 Explainable Result
                           |
                           v
                  +------------------+
                  |     BACKEND      |
                  +--------+---------+
                           |
                           v
                       FRONTEND
```

---

# 3. One-Bidder Identity Intelligence

DIXY analyzes the evidence of a single bidder.

Conceptually:

```text
                         PAN
                      /   |   \
                     /    |    \
                   GST   ITR   UDYAM
                    |      |      |
                    |      |      |
                   MCA --- + -----+
```

The Identity Graph represents relationships between pieces of bidder
evidence.

The objective is not simply to ask whether two names are similar.

The objective is to determine whether the **whole collection of evidence
forms a coherent identity**.

---

# 4. Identity Graph

The current Identity Graph checks relationships such as:

### Exact identifier relationships

```text
PAN ↔ UDYAM PAN
PAN ↔ ITR PAN
GSTIN ↔ UDYAM GSTIN
```

These are deterministic relationships.

If:

```text
PAN = ABCDE1234F
UDYAM PAN = ZZZZZ9999Z
```

the result is a critical identity discrepancy.

### Name relationships

```text
PAN name ↔ MCA company name
GST legal name ↔ MCA company name
Udyam enterprise name ↔ MCA company name
PAN name ↔ ITR name
```

These use text similarity and are treated as supporting identity
evidence.

### Supporting relationships

```text
GST state ↔ MCA registered state
MCA company status
```

These provide additional context but are not by themselves proof of
identity.

---

# 5. Missing Evidence vs Conflict

DIXY distinguishes between **missing evidence** and **contradictory
evidence**.

For example:

```text
Udyam document missing
        ↓
INSUFFICIENT EVIDENCE
```

does not automatically mean:

```text
FAIL
```

But:

```text
PAN = ABCDE1234F
Udyam PAN = ZZZZZ9999Z
        ↓
IDENTITY DISCREPANCY
```

is an actual conflict.

This distinction is important for procurement decision support.

---

# 6. OCR / Document Intelligence Boundary

The OCR/document-intelligence module is responsible for:

```text
Document
   ↓
OCR
   ↓
Field extraction
   ↓
Structured bidder JSON
```

DIXY AI receives the structured output.

The AI does not need to directly process the original document for the
current identity-analysis workflow.

Example input:

```json
{
  "pan": {
    "name": "ABC Technologies Pvt Ltd",
    "pan": "ABCDE1234F"
  },

  "gst": {
    "legal_name": "ABC Technologies Private Limited",
    "gstin": "19ABCDE1234F1Z5",
    "state": "West Bengal"
  }
}
```

Additional supported evidence can include:

- Udyam
- MCA
- CIN
- ITR
- Authorized person / authorized signatory
- Address
- Other document fields

---

# 7. Dynamic Identity Extraction

The engine currently supports dynamic extraction for several document
types.

### PAN

```text
PAN number
PAN-holder name
Address
```

### GST

```text
GSTIN
Legal name
Trade name
Address
State
```

### Udyam

```text
Udyam number
Enterprise name
PAN
GSTIN
Address
```

### MCA

```text
CIN
Company name
Registered state
Company status
Registered office address
```

### ITR

```text
PAN
Name
Acknowledgement number
Assessment year
Turnover
Address
```

### Authorized person / signatory

```text
Name
PAN
Aadhaar
Designation
```

### Important ITR rule

The ITR acknowledgement number is **not a PAN**.

DIXY treats:

```text
ITR PAN
```

as the identity field that can be compared with the bidder PAN.

The:

```text
ITR acknowledgement number
```

is a separate filing identifier.

If the ITR PAN is not extracted, DIXY should treat that relationship as
insufficient evidence rather than inventing a match or mismatch.

---

# 8. Deterministic Checks vs AI/ML

DIXY intentionally separates deterministic validation from
similarity-based intelligence.

## Deterministic checks

Used where an exact relationship is known:

- PAN format
- GSTIN format
- PAN embedded in GSTIN
- PAN ↔ Udyam PAN
- PAN ↔ ITR PAN
- GSTIN ↔ Udyam GSTIN
- State equality
- Date/threshold/arithmetic rules when applicable

Example:

```text
Bidder PAN:
ABCDE1234F

GSTIN:
19ABCDE1234F1Z5

PAN embedded in GSTIN:
ABCDE1234F

Result:
CONSISTENT
```

## Similarity / ML-supported checks

Used where exact equality is too strict:

- Company names
- Address similarity
- Business activity similarity
- OCR spelling variation
- Abbreviation/suffix variation

Example:

```text
ABC Technologies Pvt Ltd
        vs
ABC Technologies Private Limited
```

These may be treated as the same identity based on similarity and
supporting evidence.

---

# 9. Entity Resolution Model

The project contains an initial Entity Resolution baseline:

```text
dixy_entity_resolution_model.pkl
dixy_entity_resolution_config.pkl
```

The model was developed using the Indian company registration dataset as
a prototype training source.

Dataset characteristics used during development:

- Approximately 1.99 million company records
- CIN
- Company name
- Address
- State
- Business activity
- Company status and related fields

The prototype model uses features including:

```text
Name similarity
Address similarity
State match
Business activity similarity
```

The current model is a supporting entity-resolution component, not the
sole decision-maker.

### Important evaluation note

The initial training/evaluation pairs were generated synthetically.

Therefore, very high prototype evaluation scores should **not** be
presented as production-world accuracy.

Real deployment requires representative labelled bidder/document pairs
and validation on realistic OCR variations.

---

# 10. Cross-Document Consistency

The current engine compares important identity fields across documents.

Examples:

```text
PAN name ↔ GST legal name
PAN name ↔ Udyam enterprise name
GST legal name ↔ Udyam enterprise name
```

Current prototype interpretation:

```text
Similarity >= 85
        ↓
CONSISTENT

70 <= Similarity < 85
        ↓
REVIEW

Similarity < 70
        ↓
DISCREPANCY
```

These thresholds are prototype values and can be calibrated using real
labelled data.

---

# 11. Identifier Checks

DIXY performs deterministic identifier checks separately from name
similarity.

Current example:

```text
PAN ↔ GSTIN embedded PAN
```

A mismatch is treated as a high-severity identity signal.

Important distinction:

```text
VALID FORMAT
       ≠
GOVERNMENT VERIFIED
```

Format validation only checks structural validity.

Authoritative verification must come from the Verification module.

---

# 12. Verification Module

Government/authoritative verification is a separate responsibility.

```text
              GOVERNMENT /
          AUTHORITATIVE SOURCES
                    |
                    v
          +---------------------+
          | Verification Module |
          +----------+----------+
                     |
                     | Verification JSON
                     v
                 DIXY AI
```

The Verification module is responsible for obtaining authoritative
results where authorized services are available.

Examples may include:

- PAN verification
- GST verification
- Udyam verification
- MCA verification
- Other authorized verification services

DIXY AI does **not** invent authoritative verification results.

It analyzes the verification results supplied to it.

If authoritative verification says:

```text
GSTIN ↔ PAN = MISMATCH
```

DIXY treats that as a significant verification signal even if simple
text similarity looks normal.

---

# 13. Anomaly Detection

The anomaly engine currently uses rule-based signals.

Examples:

```text
Critical identifier conflict
        ↓
High anomaly signal
```

```text
Low company-name similarity
        ↓
Name mismatch signal
```

```text
Invalid identifier format
        ↓
Anomaly signal
```

```text
Missing evidence
        ↓
Evidence gap
```

The current anomaly system is intentionally a lightweight prototype.

It should be described as:

> **Rule-based anomaly detection and identity-risk analysis**

rather than as a trained fraud-detection model.

DIXY should report:

```text
IDENTITY DISCREPANCY
```

or:

```text
MANUAL VERIFICATION REQUIRED
```

rather than claiming:

```text
FRAUD DETECTED
```

---

# 14. Risk Scoring

The current prototype converts detected signals into risk points and a
risk level.

```text
0 - 19      LOW
20 - 39     MEDIUM
40 - 59     HIGH
60+         CRITICAL
```

Risk is a decision-support signal.

It is not an automatic bidder disqualification mechanism.

### Current scoring direction

Critical identifier conflicts receive stronger weight than normal name
variations.

The system also considers:

- Identifier conflicts
- Name conflicts
- Invalid formats
- Identity Graph conflicts
- Authoritative verification failures

The scoring system will be calibrated further after broader end-to-end
testing.

---

# 15. Evidence Coverage

DIXY tracks whether expected identity evidence is available.

Example:

```text
PAN     ✓
GST     ✓
Udyam   ✗
```

This may result in reduced evidence coverage.

Missing Udyam evidence does not automatically mean the bidder is
non-compliant because whether Udyam is required depends on the tender
and bidder context.

---

# 16. Master AI Function

The current internal entry point is:

```python
dixy_analyze(
    bidder_json,
    verification_results=None
)
```

It combines:

```text
Identity extraction
       ↓
Cross-document consistency
       ↓
Identifier checks
       ↓
Identity Graph
       ↓
Risk analysis
       ↓
Anomaly analysis
       ↓
Evidence analysis
       ↓
Verification analysis
       ↓
Final recommendation
```

The backend should eventually interact with the AI through FastAPI
rather than directly importing Python functions.

---

# 17. Current AI Output

The master function returns structured JSON containing:

```text
status
identity_consistency_score
evidence_coverage
available_documents
missing_documents
risk_points
risk_level
consistency_checks
identifier_checks
identity_graph
verification_analysis
anomaly_analysis
findings
recommendation
explanation
```

This allows the frontend to show both a high-level decision-support
summary and detailed evidence.

---

# 18. Human-Readable Example

A deliberate conflict test produced the following type of result:

```text
OVERALL STATUS
REVIEW

RISK LEVEL
CRITICAL

ANOMALY LEVEL
HIGH

RECOMMENDATION
MANUAL VERIFICATION REQUIRED
```

Detected conflicts included:

```text
PAN ↔ UDYAM PAN
CRITICAL

PAN ↔ ITR PAN
CRITICAL

GSTIN ↔ UDYAM GSTIN
CRITICAL

PAN name ↔ MCA company name
HIGH

GST legal name ↔ MCA company name
HIGH

PAN name ↔ ITR name
HIGH
```

At the same time, some evidence remained consistent:

```text
PAN ↔ GSTIN
CONSISTENT

GST state ↔ MCA registered state
CONSISTENT

MCA company status
ACTIVE
```

The system therefore identifies a potentially split identity pattern and
recommends human verification rather than automatically rejecting the
bidder.

---

# 19. Example Identity Conflict

Example:

```text
Submitted PAN
ABC Technologies Pvt Ltd
ABCDE1234F

Submitted GST
ABC Technologies Private Limited
19ABCDE1234F1Z5

Udyam
XYZ Industries Private Limited
PAN = ZZZZZ9999Z

MCA
XYZ Industries Private Limited

ITR
XYZ Industries Private Limited
PAN = ZZZZZ9999Z
```

DIXY can reason:

```text
PAN/GST evidence
        ↓
ABC Technologies

Udyam/MCA/ITR evidence
        ↓
XYZ Industries

        ↓

IDENTITY CONFLICT
        ↓
MANUAL VERIFICATION REQUIRED
```

This is the intended role of the Identity Graph.

---

# 20. Backend Integration

The DIXY AI service is exposed through FastAPI.

Current endpoint:

```text
POST /api/ai/analyze
```

Request structure:

```json
{
  "bidder_data": {
    "pan": {},
    "gst": {},
    "udyam": {}
  },

  "verification_results": {}
}
```

`verification_results` is optional.

Internally the API calls:

```python
dixy_analyze(
    request.bidder_data,
    request.verification_results
)
```

and returns the analysis JSON.

---

# 21. FastAPI Service

Current service file:

```text
dixy_api.py
```

Current service metadata:

```text
Service:
DIXY AI

Version:
DIXY-AI-v0.2

Health endpoint:
GET /health

Analysis endpoint:
POST /api/ai/analyze
```

Health response:

```json
{
  "status": "healthy",
  "service": "dixy-ai",
  "version": "DIXY-AI-v0.1"
}
```

The FastAPI service has been tested in the development environment.

When the AI engine is updated, the Uvicorn process should be restarted
so it loads the latest engine code.

---

# 22. Backend Integration Flow

```text
1. Bidder uploads documents
            ↓
2. OCR service processes documents
            ↓
3. OCR produces structured JSON
            ↓
4. Backend receives OCR JSON
            ↓
5. Backend calls DIXY AI
            ↓
6. DIXY AI performs identity analysis
            ↓
7. Verification results are supplied when available
            ↓
8. DIXY returns analysis JSON
            ↓
9. Backend stores result
            ↓
10. Frontend displays result
```

The intended service boundary is:

```text
Java/Kotlin Backend
        |
        | HTTP POST
        v
   DIXY FastAPI
        |
        v
 dixy_analyze()
        |
        v
 Analysis JSON
        |
        v
Java/Kotlin Backend
```

The backend should **not** directly import Python modules.

---

# 23. Team Responsibilities

## OCR / Document Intelligence

Responsible for:

```text
Document
   ↓
OCR
   ↓
Field extraction
   ↓
Structured bidder JSON
```

Key question:

> **What does this document say?**

---

## Verification / Government Integration

Responsible for:

```text
Identifier
   ↓
Appropriate authoritative source
   ↓
Verification result
```

Key question:

> **Can this information be verified from an appropriate source?**

---

## AI / Compliance Intelligence

Responsible for:

```text
All bidder evidence
        ↓
Identity relationships
        ↓
Consistency
        ↓
Discrepancies
        ↓
Anomalies
        ↓
Risk
        ↓
Explainable recommendation
```

Key question:

> **Do all these pieces of information actually belong together, and
> what should the officer review?**

---

## Backend

Responsible for orchestration:

```text
OCR
 |
 +----> DIXY AI
 |
 +----> Verification
 |
 +----> Database
 |
 +----> Frontend
```

---

## Frontend

Responsible for presenting the AI output clearly.

The frontend should not independently calculate the DIXY risk score.

---

## Procurement Officer

Responsible for the final decision.

```text
AI ASSISTS
     ↓
HUMAN DECIDES
```

---

# 24. Frontend Display Recommendation

For a consistent bidder:

```text
Identity Status
CONSISTENT

Risk
LOW

Evidence Coverage
100%

Findings
No significant identity conflicts

Recommendation
IDENTITY CONSISTENT
```

For a conflicting bidder:

```text
Identity Status
REVIEW

Risk
CRITICAL

Anomaly
HIGH

Critical Findings
3 identifier conflicts

High Findings
3 name conflicts

Recommendation
MANUAL VERIFICATION REQUIRED
```

The UI should allow the officer to expand each finding and see:

```text
Check
Severity
Submitted values
Similarity / relationship result
Reason
Recommended verification action
```

---

# 25. Repository Structure

Recommended AI module:

```text
ai/
│
├── dixy_ai_engine.py
├── dixy_api.py
├── dixy_entity_resolution_model.pkl
├── dixy_entity_resolution_config.pkl
├── requirements.txt
└── README.md
```

Development-only artifacts such as backup engine versions and generated
training-pair CSV files should not be treated as production AI module
files.

Examples:

```text
Do not commit as production module:

dixy_ai_engine_v0.1_*_backup.py
dixy_entity_matching_pairs.csv
dixy_entity_matching_features.csv
```

---

# 26. Model Artifact Usage

The `.pkl` model is a trained model artifact.

It is useful only when the runtime Python code loads it and calls the
trained model for prediction.

The intended deployment pattern is:

```text
ai/
├── dixy_ai_engine.py
├── dixy_entity_resolution_model.pkl
└── dixy_entity_resolution_config.pkl
```

The Python service should load the model using a path relative to the AI
module/deployment directory rather than a Kaggle-specific path such as:

```text
/kaggle/working/...
```

The current Identity Graph also contains deterministic checks that do
not require the ML model.

For example:

```text
PAN ↔ Udyam PAN
PAN ↔ ITR PAN
GSTIN ↔ Udyam GSTIN
PAN ↔ GSTIN embedded PAN
```

These relationships should remain deterministic.

The ML/entity-resolution model is a supporting component for
similarity-based entity matching.

---

# 27. Current Prototype Status

## Implemented

- [x] Indian company dataset exploration
- [x] Company-name normalization
- [x] Entity-resolution baseline
- [x] Model/config artifact generation
- [x] Dynamic identity extraction
- [x] PAN/GST/Udyam checks
- [x] MCA/CIN support
- [x] ITR support
- [x] Authorized-person field support
- [x] Cross-document consistency
- [x] Identifier relationship checks
- [x] Identity Graph
- [x] Identity Graph → risk integration
- [x] Identity Graph → anomaly integration
- [x] Identity Graph → final recommendation integration
- [x] Evidence coverage
- [x] Verification-result processing
- [x] FastAPI service
- [x] `/health` endpoint
- [x] `/api/ai/analyze` endpoint
- [x] Clean-bidder and deliberate-conflict testing

## Next development priorities

- [ ] Improve risk-score calibration and avoid double-counting related
      conflicts
- [ ] Improve explainability and recommended actions
- [ ] Add more document relationships
- [ ] Tender requirement intelligence
- [ ] Compliance-rule engine integration
- [ ] Final backend integration
- [ ] Frontend integration
- [ ] End-to-end test suite
- [ ] Deployment hardening

---

# 28. Important Prototype Limitations

The current system is a prototype.

### Entity Resolution

The initial model was evaluated using synthetically generated training
pairs.

It should not be presented as production-grade accuracy.

### Government Verification

DIXY does not claim that an identifier is government-verified merely
because its format is valid.

Authoritative verification depends on the Verification module and
available authorized services.

### Identity Graph

An identity conflict is an **identity-risk signal**, not proof of fraud.

### Compliance

Identity consistency is not the same thing as tender compliance.

A bidder can have a consistent identity and still fail a tender
requirement.

---

# 29. Identity vs Verification vs Compliance vs Decision

DIXY separates these concepts:

## Identity

```text
Do the documents appear to belong
to the same underlying bidder?
```

## Verification

```text
Does an appropriate authoritative source
confirm the submitted information?
```

## Compliance

```text
Does the bidder satisfy the
requirements of this specific tender?
```

## Decision

```text
Does the Procurement Officer accept
or reject the bidder based on the evidence?
```

This separation prevents the AI layer from becoming an uncontrolled
automatic procurement decision-maker.

---

# 30. Quick Integration Summary

For the backend team:

```text
YOU RECEIVE
    ↓
OCR JSON
    +
Verification JSON (when available)

YOU SEND
    ↓
POST /api/ai/analyze

DIXY RETURNS
    ↓
Status
Identity Score
Evidence Coverage
Risk Level
Risk Points
Consistency Checks
Identifier Checks
Identity Graph
Verification Analysis
Anomaly Analysis
Findings
Recommendation
Explanation

YOU DISPLAY / STORE
    ↓
Frontend + Database
```

The most important rule:

> **Do not couple Java/Kotlin code to individual Python functions. Use
> the FastAPI API as the integration boundary.**

---

# 31. DIXY Design Principle

DIXY AI implements a clear separation of concerns and human-centered decision support:

```text
                     BIDDER DOCUMENTS
                            |
                            v
                    +------------------+
                    | OCR / Extraction |
                    |   (What does it  |
                    |    document say?)|
                    +--------+---------+
                             |
                Structured Evidence
                             |
         +---────────────────┬───────────────────+
         |                   |                   |
         v                   v                   v
    Consistency         Identity Graph       Verification
      Analysis          Analysis (1 bidder)  Analysis
         |                   |                   |
         +───────────────────┼───────────────────+
                             |
                    Anomaly Detection
                             |
                      Risk Scoring
                             |
                    Compliance Checks
                             |
              ┌──────────────┴──────────────┐
              |                             |
              v                             v
    Consistent Identity        Review Required
    Low Risk                   Manual Verification
              |                             |
              └──────────────┬──────────────┘
                             |
                             v
              PROCUREMENT OFFICER DECISION
                             |
                      FINAL DETERMINATION
```

## Core Principle

> **DIXY AI assists the officer; it does not replace the officer.**

DIXY provides explainable, evidence-based recommendations backed by:

- **Deterministic checks** for hard identity rules (PAN, GSTIN, etc.)
- **Similarity analysis** for name and business information matching
- **Identity Graph reasoning** connecting related documents and evidence
- **Risk scoring** based on weighted anomaly signals
- **Verification integration** incorporating authoritative source results
- **Clear explanations** of why each finding matters

The Procurement Officer retains full authority to accept, reject, or request additional verification for any bidder, regardless of the AI analysis.
