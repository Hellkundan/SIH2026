# DIXY AI Engine

## DIXY-AI-v0.1

AI-powered bidder identity consistency and compliance intelligence engine for the DIXY procurement platform.

---

## 1. What is DIXY AI?

DIXY AI is the intelligence layer of the DIXY procurement platform.

It receives structured information extracted from bidder documents and analyzes whether the available information is internally consistent.

The engine currently focuses on:

- Cross-document identity consistency
- Company-name similarity
- Identifier consistency
- PAN ↔ GSTIN relationship checking
- Identifier format validation
- Anomaly detection
- Risk scoring
- Evidence coverage
- Explainable recommendations
- Consumption of authoritative verification results

DIXY AI is a decision-support system.

It does NOT automatically approve, reject, disqualify, or declare a bidder fraudulent.

Final procurement decisions remain with the Procurement Officer.

---

## 2. High-Level Architecture

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
                           | HTTP
                           v
                  +------------------+
                  |    DIXY AI       |
                  |    FastAPI       |
                  +--------+---------+
                           |
                           v
                  +------------------+
                  | dixy_analyze()   |
                  +--------+---------+
                           |
             +-------------+-------------+
             |             |             |
             v             v             v
        Consistency   Identifier    Anomaly
          Engine        Checks       Engine
             |             |             |
             +-------------+-------------+
                           |
                           v
                    Risk / Scoring
                           |
                           v
                  AI Result JSON
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

## 3. Verification Module

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

The Verification module is responsible for obtaining authoritative results.

Examples may include:

- PAN verification
- GST verification
- Udyam verification
- MCA verification
- Other authorized verification services

DIXY AI does NOT invent or simulate authoritative verification results.

It only analyzes verification results supplied to it.

---

## 4. Team Responsibilities

### OCR / Document Intelligence Team

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

The OCR team sends the extracted JSON to the backend.

They do not need to call individual DIXY AI functions.

### Verification / Government Integration Team

Responsible for:

```text
Identifier
   ↓
Authoritative source
   ↓
Verification result
```

The verification team provides verification results to the backend/DIXY AI.

They should not duplicate the AI's discrepancy and risk analysis.

### DIXY AI Team

Responsible for:

- Identity consistency
- Entity/name matching
- Cross-document relationships
- Identifier consistency
- Discrepancy detection
- Anomaly detection
- Risk scoring
- Explainable recommendations

### Java / Kotlin Backend Team

The backend is the main orchestrator.

The backend should connect:

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

The backend should NOT directly import the Python AI module.

The intended architecture is:

```text
Java/Kotlin
    |
    | HTTP POST
    v
FastAPI
    |
    v
dixy_analyze()
    |
    v
JSON result
    |
    v
Java/Kotlin
```

---

## 5. Current Master AI Function

The current internal entry point is:

```python
dixy_analyze(
    bidder_json,
    verification_results=None
)
```

The backend should eventually interact with the AI through the FastAPI API rather than directly importing this Python function.

The internal functions should NOT be called individually by the backend.

These are internal AI components:

```python
normalize_text()
text_similarity()
extract_identity_data()
check_cross_document_consistency()
check_important_identifiers()
calculate_weighted_risk()
dixy_anomaly_check()
calculate_evidence_summary()
analyze_verification_results()
```

The backend should use the FastAPI API as the integration boundary.

---

## 6. AI Input

The AI receives information belonging to ONE bidder.

It does NOT compare two bidders as its primary workflow.

Example:

```json
{
  "pan": {
    "name": "ABC Technologies Pvt Ltd",
    "pan": "ABCDE1234F"
  },

  "gst": {
    "legal_name": "ABC Technologies Private Limited",
    "gstin": "19ABCDE1234F1Z5"
  },

  "udyam": {
    "enterprise_name": "ABC Technology Private Limited"
  }
}
```

This is only an example.

The real OCR output may contain additional documents and fields.

Possible future fields may include:

- Udyam number
- CIN
- MCA company name
- ITR information
- Authorized person information
- Address
- Other bidder documents

The AI engine will be expanded to support additional document relationships.

---

## 7. How the AI Uses the Input

Current pipeline:

```text
OCR JSON
    ↓
Identity Extraction
    ↓
Text Normalization
    ↓
Name Similarity
    ↓
Cross-Document Consistency
    ↓
Identifier Checks
    ↓
Anomaly Detection
    ↓
Risk Scoring
    ↓
Verification Analysis
    ↓
Final Recommendation
```

---

## 8. Current Identity Checks

### Name consistency

```text
PAN name
     ↕
GST legal name

PAN name
     ↕
Udyam enterprise name

GST legal name
     ↕
Udyam enterprise name
```

Similarity is calculated using text similarity.

### PAN format

The engine checks whether the PAN follows the expected structural format.

This is only a format check.

```text
VALID FORMAT
    ≠
GOVERNMENT VERIFIED
```

### GSTIN format

The engine checks whether the GSTIN follows the expected structural format.

Again:

```text
VALID FORMAT
    ≠
OWNERSHIP VERIFIED
```

### PAN ↔ GSTIN relationship

The engine checks whether the PAN supplied separately matches the PAN embedded inside the GSTIN.

Example:

```text
PAN:
ABCDE1234F

GSTIN:
19ABCDE1234F1Z5

Embedded PAN:
ABCDE1234F

Result:
CONSISTENT
```

If they differ:

```text
PAN:
ABCDE1234F

GSTIN:
19XYZXY9876A1Z5

Embedded PAN:
XYZXY9876A

Result:
CRITICAL DISCREPANCY
```

---

## 9. Risk Levels

Current prototype risk classification:

```text
0 - 19     LOW
20 - 39    MEDIUM
40 - 59    HIGH
60+        CRITICAL
```

Risk is a decision-support signal.

It is NOT an automatic bidder disqualification.

---

## 10. Example AI Result

For a consistent bidder:

```json
{
  "status": "CONSISTENT",
  "identity_consistency_score": 91.11,
  "evidence_coverage": 100.0,
  "available_documents": [
    "PAN",
    "GST",
    "UDYAM"
  ],
  "missing_documents": [],
  "risk_points": 0,
  "risk_level": "LOW",
  "recommendation": "IDENTITY CONSISTENT",
  "explanation": "No significant identity conflicts were detected in the available evidence."
}
```

---

## 11. Example Conflict Result

If PAN and GSTIN conflict:

```json
{
  "status": "REVIEW",
  "risk_level": "CRITICAL",
  "recommendation": "MANUAL VERIFICATION REQUIRED",
  "explanation": "A significant identity conflict was detected either within the submitted documents or in authoritative verification results."
}
```

The backend should pass this result to the frontend for display.

---

## 12. Verification Results Input

Verification results are optional in the current prototype.

Example:

```json
{
  "pan_verification": {
    "status": "VERIFIED",
    "source": "PAN_SERVICE"
  },

  "gst_verification": {
    "status": "VERIFIED",
    "pan_match": true,
    "source": "GST_SERVICE"
  },

  "udyam_verification": {
    "status": "VERIFIED",
    "source": "UDYAM_SERVICE"
  }
}
```

DIXY can combine this with OCR-derived evidence.

If authoritative verification reports a mismatch:

```text
OCR:
Names appear consistent

        BUT

Verification:
GSTIN ↔ PAN mismatch

        ↓

DIXY:
MANUAL VERIFICATION REQUIRED
```

Authoritative verification takes priority over simple text similarity.

---

## 13. Important Architecture Principle

DIXY separates four concepts.

### Identity

```text
Do the documents appear to belong
to the same underlying bidder?
```

### Verification

```text
Does an authoritative source confirm
the submitted information?
```

### Compliance

```text
Does the bidder satisfy the
requirements of this specific tender?
```

### Decision

```text
Procurement Officer makes the final decision.
```

The AI assists the officer.

---

## 14. Backend Integration

The planned FastAPI endpoint is:

```text
POST /api/ai/analyze
```

The backend will eventually send:

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

The FastAPI service will internally call:

```python
dixy_analyze(
    bidder_data,
    verification_results
)
```

and return the resulting JSON.

The exact HTTP request/response contract will be frozen when the FastAPI service is implemented.

---

## 15. Backend Integration Flow

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
6. DIXY AI analyzes bidder
            ↓
7. DIXY returns analysis JSON
            ↓
8. Backend stores result
            ↓
9. Frontend displays result
```

If verification results are available:

```text
OCR JSON
    +
Verification JSON
        ↓
     DIXY AI
        ↓
   Analysis JSON
```

---

## 16. Frontend Integration

The frontend should NOT calculate risk itself.

The frontend should display the result received from the backend.

Suggested display:

```text
Identity Consistency
91.11 / 100

Risk
LOW

Evidence Coverage
100%

Status
CONSISTENT

Findings
No significant conflicts

Recommendation
IDENTITY CONSISTENT
```

For conflicts:

```text
Risk
CRITICAL

Status
REVIEW

Finding
PAN ↔ GSTIN conflict

Recommendation
MANUAL VERIFICATION REQUIRED
```

---

## 17. Current Repository Files

```text
ai/
│
├── dixy_ai_engine.py
├── dixy_entity_resolution_model.pkl
├── dixy_entity_resolution_config.pkl
├── requirements.txt
└── README.md
```

---

## 18. Entity Resolution Model

The repository also contains the initial DIXY Entity Resolution model.

Files:

```text
dixy_entity_resolution_model.pkl
dixy_entity_resolution_config.pkl
```

The model was developed as an initial entity-resolution baseline.

Its purpose is to help estimate whether identity records/names appear to represent the same underlying entity.

It is a supporting component of the larger DIXY identity intelligence system.

The current prototype was trained/evaluated using synthetic pair generation.

Therefore the current evaluation results should NOT be interpreted as production-world accuracy.

---

## 19. Current Version Status

Version:

```text
DIXY-AI-v0.1
```

Status:

```text
Prototype / Development
```

Implemented:

- Identity extraction
- Name normalization
- Name similarity
- Cross-document consistency
- PAN format validation
- GSTIN format validation
- PAN ↔ GSTIN consistency
- Anomaly detection
- Risk scoring
- Evidence coverage
- Verification-result processing
- Master AI analysis function

Next:

- Dynamic support for additional OCR document types
- Expanded identity graph
- Additional identifier relationships
- Final FastAPI service
- Final API contract
- Backend integration
- Production hardening

---

## 20. Important Disclaimer

DIXY AI provides decision-support intelligence.

It does not independently determine legal eligibility, bidder fraud, or final procurement qualification.

All high-risk findings should be treated as signals for verification/review.

The final procurement decision remains with the authorized Procurement Officer.

---

## 21. Quick Integration Summary

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
Verification Analysis
Anomaly Analysis
Findings
Recommendation
Explanation

YOU DISPLAY/STORE
    ↓
Frontend + Database
```

Do not couple Java/Kotlin code to individual Python functions.

Use the FastAPI API as the integration boundary.
