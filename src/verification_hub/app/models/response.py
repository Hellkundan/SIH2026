from datetime import datetime
from pydantic import BaseModel,Field
from typing import List,Any,Dict,Optional
from app.models.enums import VerificationType, VerificationStatus,ErrorState

class VerificationEvidence(BaseModel):
    provider : str
    source : str
    matched_identifier : Optional[str] = None
    details : Dict[str,Any]=Field(default_factory=dict)

class VerificationResponse(BaseModel):
    bidder_id: str
    verification_type: VerificationType #take from enums
    status: VerificationStatus #take from enums
    source: str
    timestamp: datetime
    evidence : VerificationEvidence  #take from enums
    error_state : ErrorState = ErrorState.NONE  #take from enums
    error_message: Optional[str] = None
    confidence: float = 1.0

class BatchVerificationResponse(BaseModel):
    results : List[VerificationResponse]
