import re
from typing import List

from .schemas import TenderRequirement


def _find_value(patterns: List[str], text: str) -> str | None:
    for pattern in patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            return match.group(1).strip()
    return None


def extract_tender_requirements(text: str) -> dict:
    """
    Extract common tender requirements from raw tender text.

    This is a prototype rule-based extractor.
    It does not make the final compliance decision.
    """

    if not text or not text.strip():
        return {
            "eligibility_requirements": [],
            "financial_requirements": [],
            "experience_requirements": [],
            "mandatory_documents": [],
            "special_conditions": [],
            "attention_items": [],
        }

    text = re.sub(r"\s+", " ", text).strip()

    eligibility = []
    financial = []
    experience = []
    documents = []
    special_conditions = []
    attention_items = []

    # ---------------------------------------------------------
    # GST
    # ---------------------------------------------------------
    if re.search(r"\bGST\b|GSTIN|GST registration", text, re.IGNORECASE):
        eligibility.append(
            TenderRequirement(
                category="ELIGIBILITY",
                requirement="Valid GST registration is required",
                evidence_required="GST certificate",
            )
        )
        documents.append(
            TenderRequirement(
                category="DOCUMENT",
                requirement="GST certificate",
                evidence_required="GST certificate",
            )
        )

    # ---------------------------------------------------------
    # UDYAM / MSME
    # ---------------------------------------------------------
    
    if re.search(r"Udyam|MSME", text, re.IGNORECASE):
        udyam_applicable = bool(
            re.search(
                r"(Udyam|MSME).{0,80}(where applicable|if applicable)",
                text,
                re.IGNORECASE,
            )
        )

        eligibility.append(
            TenderRequirement(
                category="ELIGIBILITY",
                requirement=(
                    "Valid Udyam/MSME registration is required "
                    "(where applicable)"
                    if udyam_applicable
                    else "Valid Udyam/MSME registration is required"
                ),
                mandatory=not udyam_applicable,
                evidence_required="Udyam/MSME certificate",
            )
        )

        
    
    
    
    
    
    
    documents.append(
            TenderRequirement(
                category="DOCUMENT",
                requirement="Udyam/MSME certificate",
                evidence_required="Udyam/MSME certificate",
            )
        )

    # ---------------------------------------------------------
    # PAN
    # ---------------------------------------------------------
    if re.search(r"\bPAN\b|Permanent Account Number", text, re.IGNORECASE):
        documents.append(
            TenderRequirement(
                category="DOCUMENT",
                requirement="PAN document",
                evidence_required="PAN card/document",
            )
        )

    # ---------------------------------------------------------
    # Turnover
    # ---------------------------------------------------------
    turnover = _find_value(
        [
            r"(?:minimum\s+)?(?:average\s+)?annual\s+turnover"
            r".{0,100}?((?:₹|Rs\.?|INR)\s*[\d,.]+\s*(?:crore|cr|lakh|lac)?)",

            r"turnover\s+(?:of|minimum|required|at\s+least)"
            r"\s*((?:₹|Rs\.?|INR)\s*[\d,.]+\s*(?:crore|cr|lakh|lac)?)",
        ],
        text,
    )
    if turnover:
        financial.append(
            TenderRequirement(
                category="FINANCIAL",
                requirement="Minimum turnover requirement",
                value=turnover,
                evidence_required="ITR / audited financial statements",
            )
        )
        attention_items.append(
            "Turnover threshold must be verified against financial evidence."
        )

    # ---------------------------------------------------------
    # Experience
    # ---------------------------------------------------------
    experience = _find_value(
        [
            r"(?:minimum\s+)?experience"
            r".{0,80}?(\d+\+?\s*(?:years?|yrs?))",
            r"(\d+\+?\s*(?:years?|yrs?))"
            r".{0,80}?experience",
        ],
        text,
    )

    if experience:
        experience_requirements = [
            TenderRequirement(
                category="EXPERIENCE",
                requirement="Minimum relevant experience",
                value=experience,
                evidence_required="Experience certificates / work orders",
            )
        ]
    else:
        experience_requirements = []

    # ---------------------------------------------------------
    # OEM authorization
    # ---------------------------------------------------------
    if re.search(
        r"OEM\s+authorization|authorized\s+OEM|OEM\s+certificate",
        text,
        re.IGNORECASE,
    ):
        documents.append(
            TenderRequirement(
                category="DOCUMENT",
                requirement="OEM authorization is required",
                evidence_required="OEM authorization certificate",
            )
        )
        attention_items.append(
            "OEM authorization is a mandatory document requiring verification."
        )

    # ---------------------------------------------------------
    # ITR
    # ---------------------------------------------------------
    if re.search(
        r"\bITR\b|income\s+tax\s+return",
        text,
        re.IGNORECASE,
    ):
        documents.append(
            TenderRequirement(
                category="DOCUMENT",
                requirement="Income Tax Return (ITR)",
                evidence_required="ITR acknowledgement / return document",
            )
        )

    # ---------------------------------------------------------
    # Local content / Make in India
    # ---------------------------------------------------------
    if re.search(
        r"Class[- ]I\s+local\s+supplier|local\s+content|Make\s+in\s+India",
        text,
        re.IGNORECASE,
    ):
        special_conditions.append(
            TenderRequirement(
                category="SPECIAL_CONDITION",
                requirement="Local-content / Make in India requirement",
                evidence_required="Self-certification / supporting declaration",
            )
        )

    # ---------------------------------------------------------
    # Mandatory language
    # ---------------------------------------------------------
    mandatory_patterns = [
        r"\bmandatory\b",
        r"\bmust\b",
        r"\bshall\b",
        r"\brequired\b",
    ]

    for pattern in mandatory_patterns:
        if re.search(pattern, text, re.IGNORECASE):
            attention_items.append(
                "Tender contains mandatory eligibility/document conditions "
                "that must be checked before final decision."
            )
            break

    return {
        "eligibility_requirements": eligibility,
        "financial_requirements": financial,
        "experience_requirements": experience_requirements,
        "mandatory_documents": documents,
        "special_conditions": special_conditions,
        "attention_items": list(dict.fromkeys(attention_items)),
    }