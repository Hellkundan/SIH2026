from pydantic import BaseModel
from typing import Optional,Dict,Any

class VerificationRequest(BaseModel):
    bidder_id: str =Field(...,description="Unique bidder ID")
    verification_type : verificationtype
    identifier : Optional[str] = None
    company_name : Optional[str] = None
    pan : Optional[str] = None
    gstin : Optional[str] = None
    metadata : Dict[str, Any] = {}
