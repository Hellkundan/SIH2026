from typing import Optional
from pydantic import BaseModel, Field


class BidderVerificationRequest(BaseModel):
    bidder_id: str = Field(..., min_length=1)
    company_name: Optional[str] = None
    pan: Optional[str] = None
    gstin: Optional[str] = None
    udyam: Optional[str] = None
    epfo: Optional[str] = None
    esic: Optional[str] = None
    startup_india: Optional[str] = None
    nsic: Optional[str] = None
    oem: Optional[str] = None
    blacklist: Optional[str] = None
    aadhaar: Optional[str] = None
