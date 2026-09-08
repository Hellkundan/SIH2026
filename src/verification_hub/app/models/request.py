from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from app.models.enums import VerificationType

class VerificationRequest(BaseModel):
    bidder_id: str = Field(...,description="Unique bidder ID")
    verification_type: VerificationType
    identifier: Optional[str] = None
    company_name: Optional[str] = None
    pan: Optional[str] = None
    gstin: Optional[str] = None
    metadata: Dict[str, Any] = Field(default_factory=dict)

class BatchVerificationRequest(BaseModel):
    checks: List[VerificationRequest]