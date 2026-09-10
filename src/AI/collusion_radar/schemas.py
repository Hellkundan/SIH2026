from dataclasses import dataclass, field
from typing import Dict, List, Optional


@dataclass
class Bidder:
    bidder_id: str
    company_name: str

    pan: Optional[str] = None
    gstin: Optional[str] = None
    udyam: Optional[str] = None

    address: Optional[str] = None
    phone: Optional[str] = None
    email: Optional[str] = None

    authorized_person: Optional[str] = None
    bank_account: Optional[str] = None

    document_hashes: List[str] = field(default_factory=list)


@dataclass
class CollusionSignal:
    bidder_a: str
    bidder_b: str
    signal_type: str
    value: str
    weight: int
    explanation: str


@dataclass
class CollusionFinding:
    bidder_ids: List[str]
    score: int
    risk_level: str
    signals: List[Dict]
    explanation: str