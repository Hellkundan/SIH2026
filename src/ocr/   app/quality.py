"""
Step 6: Confidence scoring and missing-field / low-quality detection.

Decides whether a document should be flagged instead of silently
accepted, per the Definition of Done: "Unreadable documents are flagged
instead of silently accepted."
"""

from typing import List
from .schemas import DocumentQualitySignals, ExtractedField

BLUR_THRESHOLD = 50.0          # below this, image is likely too blurry
MIN_OCR_CONFIDENCE = 0.4       # below this, OCR text is unreliable

REQUIRED_FIELDS_BY_TYPE = {
    "PAN": ["PAN"],
    "GST": ["GSTIN"],
    "UDYAM": ["UDYAM_NUMBER"],
    "EPFO": ["UAN"],
}


def assess_quality(
    ocr_confidence: float,
    blur: float,
    document_type: str,
    extracted_fields: List[ExtractedField],
) -> DocumentQualitySignals:
    warnings = []

    if blur < BLUR_THRESHOLD:
        warnings.append(f"Image appears blurry (score={blur:.1f}, threshold={BLUR_THRESHOLD}).")

    if ocr_confidence < MIN_OCR_CONFIDENCE:
        warnings.append(f"Low OCR confidence ({ocr_confidence}).")

    required = REQUIRED_FIELDS_BY_TYPE.get(document_type, [])
    found_names = {f.field_name for f in extracted_fields}
    missing = [f for f in required if f not in found_names]
    if missing:
        warnings.append(f"Missing expected field(s) for {document_type}: {missing}")

    is_readable = ocr_confidence >= MIN_OCR_CONFIDENCE and blur >= BLUR_THRESHOLD

    return DocumentQualitySignals(
        is_readable=is_readable,
        blur_score=round(blur, 2),
        resolution_ok=blur >= BLUR_THRESHOLD,
        warnings=warnings,
    )
