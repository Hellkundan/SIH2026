from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class TenderRequirement:
    category: str
    requirement: str
    value: Optional[str] = None
    mandatory: bool = True
    evidence_required: Optional[str] = None


@dataclass
class TenderSummary:
    tender_title: Optional[str] = None
    tendering_authority: Optional[str] = None
    submission_deadline: Optional[str] = None
    estimated_value: Optional[str] = None

    eligibility_requirements: List[TenderRequirement] = field(
        default_factory=list
    )
    financial_requirements: List[TenderRequirement] = field(
        default_factory=list
    )
    experience_requirements: List[TenderRequirement] = field(
        default_factory=list
    )
    mandatory_documents: List[TenderRequirement] = field(
        default_factory=list
    )
    special_conditions: List[TenderRequirement] = field(
        default_factory=list
    )
    attention_items: List[str] = field(default_factory=list)

    summary: Optional[str] = None