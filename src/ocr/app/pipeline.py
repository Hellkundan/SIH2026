"""
Ties Steps 1-7 together into a single call: process_document().
This is what main.py's API endpoint calls.
"""

import uuid
from typing import List

from .ingestion import validate_file, compute_document_hash, is_pdf, IngestionError
from .preprocessing import pdf_bytes_to_images, image_bytes_to_pil, clean_for_ocr, blur_score, upscale_if_small
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

    # Upscale small images (e.g. cropped ID card photos) before OCR —
    # blur_score is computed on the ORIGINAL so quality signals still
    # reflect the real source image, not an artificially sharpened copy.
    pages = [upscale_if_small(p) for p in pages]

    # Step 3: OCR — try both raw and cleaned versions of each page
    raw_text_parts, raw_confs = [], []
    cleaned_text_parts, cleaned_confs = [], []
    blur_scores = []

    for page in pages:
        raw_text_attempt, raw_conf = run_ocr(page)
        raw_text_parts.append(raw_text_attempt)
        raw_confs.append(raw_conf)

        cleaned = clean_for_ocr(page)
        cleaned_text_attempt, cleaned_conf = run_ocr(cleaned)
        cleaned_text_parts.append(cleaned_text_attempt)
        cleaned_confs.append(cleaned_conf)

        blur_scores.append(blur_score(page))

    raw_full_text = "\n".join(raw_text_parts).strip()
    cleaned_full_text = "\n".join(cleaned_text_parts).strip()
    avg_raw_conf = round(sum(raw_confs) / len(raw_confs), 3) if raw_confs else 0.0
    avg_cleaned_conf = round(sum(cleaned_confs) / len(cleaned_confs), 3) if cleaned_confs else 0.0
    avg_blur = round(sum(blur_scores) / len(blur_scores), 2) if blur_scores else 0.0

    # Step 4: classify BOTH versions, then decide which OCR result to use.
    # OCR confidence alone is unreliable — a heavily-thresholded image can
    # produce garbage that Tesseract is confidently wrong about. Instead,
    # prefer whichever version the classifier actually recognizes; only
    # fall back to raw OCR confidence if neither version classifies.
    raw_doc_type, raw_class_conf, _ = classify_document(raw_full_text)
    cleaned_doc_type, cleaned_class_conf, _ = classify_document(cleaned_full_text)

    raw_known = raw_doc_type != "UNKNOWN"
    cleaned_known = cleaned_doc_type != "UNKNOWN"

    if raw_known and not cleaned_known:
        use_raw = True
    elif cleaned_known and not raw_known:
        use_raw = False
    elif raw_known and cleaned_known:
        use_raw = raw_class_conf >= cleaned_class_conf
    else:
        use_raw = avg_raw_conf >= avg_cleaned_conf

    if use_raw:
        raw_text = raw_full_text
        avg_ocr_conf = avg_raw_conf
        doc_type, class_conf = raw_doc_type, raw_class_conf
    else:
        raw_text = cleaned_full_text
        avg_ocr_conf = avg_cleaned_conf
        doc_type, class_conf = cleaned_doc_type, cleaned_class_conf

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
