"""
Step 5: Field extraction templates.

Regex-based extraction for the key statutory identifiers. Each pattern
is the official government format, so matches are high-confidence by
construction. Layout-aware / AI extraction can be added later behind
the same `extract_fields()` interface.
"""

import re
from typing import List
from .schemas import ExtractedField

FIELD_PATTERNS = {
    "PAN": r"\b[A-Z]{5}[0-9]{4}[A-Z]\b",
    "GSTIN": r"\b\d{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}[A-Z\d]{1}\b",
    "UDYAM_NUMBER": r"\bUDYAM-[A-Z]{2}-\d{2}-\d{7}\b",
    "UAN": r"\b\d{12}\b",  # EPFO Universal Account Number, refine per real samples
}

# Fields matched by a strict official regex get high base confidence;
# UAN is a plain 12-digit number, ambiguous with other identifiers, so
# it starts lower until surrounding context is checked.
BASE_CONFIDENCE = {
    "PAN": 0.95,
    "GSTIN": 0.95,
    "UDYAM_NUMBER": 0.95,
    "UAN": 0.6,
}


def extract_fields(text: str, document_type: str) -> List[ExtractedField]:
    results: List[ExtractedField] = []
    for field_name, pattern in FIELD_PATTERNS.items():
        for match in re.finditer(pattern, text):
            results.append(
                ExtractedField(
                    field_name=field_name,
                    value=match.group(0),
                    confidence=BASE_CONFIDENCE[field_name],
                )
            )
    return results
