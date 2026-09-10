
# DIXY AI Engine

## Version
DIXY-AI-v0.1

## Purpose
AI-assisted bidder identity consistency and discrepancy analysis.

## Current Input
One bidder's OCR-extracted JSON.

Example:

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

## Current Analysis

The engine currently performs:

1. Identity field extraction
2. Company-name normalization
3. Cross-document name similarity
4. PAN format validation
5. GSTIN format validation
6. PAN-to-GSTIN embedded PAN consistency check
7. Anomaly detection
8. Risk scoring
9. Evidence coverage
10. Optional authoritative verification-result analysis

## Main Function

dixy_analyze(bidder_json, verification_results=None)

## Important Architecture Rule

The AI engine does NOT perform government verification itself.

Verification results are supplied by the Verification/Integration module.

AI:
- analyzes consistency
- detects discrepancies
- detects anomalies
- calculates risk
- generates explanations

Verification module:
- obtains authoritative verification results

Procurement Officer:
- makes the final decision

## Current Status

Prototype / development version.

Not a final legal or procurement decision engine.

## Future Integration

The engine will be exposed through a FastAPI service so the Java/Kotlin backend can call:

POST /api/ai/analyze

The API contract will be finalized in a later version.
