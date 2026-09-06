"""
Ties Steps 1-7 together into a single call: process_document().
This is what main.py's API endpoint calls.
"""

import uuid
from typing import List

from .ingestion import validate_file, compute_document_hash, is_pdf, IngestionError
from .preprocessing import pdf_bytes_to_images, image_bytes_to_pil, clean_for_ocr, blur_score
from .ocr_engine import run_ocr
from .classifier import classify_document
from .extractors import extract_fields
from .quality import assess_quality
from .schemas import DocumentIntelligenceResult


def process_document(filename: str, content: bytes) -> DocumentIntelligenceResult:
    document_id = str(uuid.uuid4())
    errors: List[str] = []

    try:
        validate_file(filename, content)
    except IngestionError as e:
        return DocumentIntelligenceResult(
            document_id=document_id,
            document_type="UNKNOWN",
            classification_confidence=0.0,
            raw_text="",
            extracted_fields=[],
            quality=assess_quality(0.0, 0.0, "UNKNOWN", []),
            document_hash="",
            processing_status="FAILED",
            errors=[str(e)],
        )

    doc_hash = compute_document_hash(content)

    # Step 2: get page images
    if is_pdf(filename):
        pages = pdf_bytes_to_images(content)
    else:
        pages = [image_bytes_to_pil(content)]

    if not pages:
        errors.append("No pages could be extracted from the document.")

    # Run OCR across all pages, concatenate text, average confidence + blur
    full_text_parts = []
    ocr_confidences = []
    blur_scores = []

    for page in pages:
        cleaned = clean_for_ocr(page)
        text, conf = run_ocr(cleaned)
        full_text_parts.append(text)
        ocr_confidences.append(conf)
        blur_scores.append(blur_score(page))

    raw_text = "\n".join(full_text_parts).strip()
    avg_ocr_conf = round(sum(ocr_confidences) / len(ocr_confidences), 3) if ocr_confidences else 0.0
    avg_blur = round(sum(blur_scores) / len(blur_scores), 2) if blur_scores else 0.0

    # Step 4: classify
    doc_type, class_conf, _matches = classify_document(raw_text)

    # Step 5: extract fields
    fields = extract_fields(raw_text, doc_type)

    # Step 6: quality assessment
    quality = assess_quality(avg_ocr_conf, avg_blur, doc_type, fields)

    status = "SUCCESS" if quality.is_readable and not errors else ("PARTIAL" if raw_text else "FAILED")

    return DocumentIntelligenceResult(
        document_id=document_id,
        document_type=doc_type,
        classification_confidence=class_conf,
        raw_text=raw_text,
        extracted_fields=fields,
        quality=quality,
        document_hash=doc_hash,
        processing_status=status,
        errors=errors,
    )
