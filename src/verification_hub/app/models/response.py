from datetime import datetime
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field

from app.models.enums import ErrorState, VerificationStatus, VerificationType


class VerificationEvidence(BaseModel):
    provider: str
    source: str
    checked_at: datetime
    evidence_type: str = "DATASET_MATCH"
    matched_identifier: Optional[str] = None
    details: Dict[str, Any] = Field(default_factory=dict)


class VerificationResponse(BaseModel):
    bidder_id: str
    verification_type: VerificationType
    status: VerificationStatus
    source: str
    timestamp: datetime
    evidence: VerificationEvidence
    error_state: ErrorState = ErrorState.NONE
    error_message: Optional[str] = None
    confidence: float = Field(default=1.0, ge=0.0, le=1.0)
    attempts: int = 1
    duration_ms: Optional[float] = None


class BatchVerificationResponse(BaseModel):
    results: List[VerificationResponse]


class BidderVerificationResponse(BaseModel):
    bidder_id: str
    overall_status: VerificationStatus
    results: List[VerificationResponse]
    summary: Dict[str, int]
