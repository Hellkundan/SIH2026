"""
Structured request/response contracts for the Document Intelligence API.

This is the schema that Member 1 (backend) and Member 2 (AI) consume.
Every response follows the shared principle: structured JSON, with
confidence and evidence attached to every extracted claim.
"""

from typing import List, Optional
from pydantic import BaseModel, Field


class ExtractedField(BaseModel):
    field_name: str          # e.g. "PAN", "GSTIN", "UDYAM_NUMBER"
    value: str
    confidence: float = Field(ge=0.0, le=1.0)


class DocumentQualitySignals(BaseModel):
    is_readable: bool
    blur_score: Optional[float] = None
    resolution_ok: bool
    warnings: List[str] = []


class DocumentIntelligenceResult(BaseModel):
    document_id: str
    document_type: str                     # GST, PAN, UDYAM, EPFO, ESIC, OEM_AUTH,
                                            # STARTUP_INDIA, NSIC, MAKE_IN_INDIA, UNKNOWN
    classification_confidence: float = Field(ge=0.0, le=1.0)
    raw_text: str
    extracted_fields: List[ExtractedField]
    quality: DocumentQualitySignals
    document_hash: str                     # sha256, for duplicate detection
    processing_status: str                 # SUCCESS, PARTIAL, FAILED
    errors: List[str] = []
