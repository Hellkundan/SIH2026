from datetime import datetime
from pydantic import BaseModel,Field
from typing import List,Any,Dict,Optional
from src.verification_hub.models.enums import VerificationType, VerificationStatus,ErrorState

class VerificationEvidence(BaseModel):
    provider : str
    source : str
    matched_identifier : Optional[str] = None
    details : Dict[str,any]=Field(default_factory=dict)

class VerificationResponse(BaseModel):
    bidder_id: str
    verification_type: VerificationType #take from enums
    status: verification_status #take from enums
    source: str
    timestamp: datetime
    evidence : VerificationEvidence  #take from enums
    error_state : ErrorState = ErroState.NONE  #take from enums
    error_message: Optional[str] = None

class BatchVerificationResponse(BaseModel):
    results : List[VerificationResponse]
