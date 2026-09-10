import json
import re

from .schemas import TenderSummary, TenderRequirement
from .extractor import extract_tender_requirements


def extract_tender_metadata(text: str) -> dict:
    """Extract basic tender metadata from raw tender text."""

    if not text or not text.strip():
        return {
            "tender_title": None,
            "tendering_authority": None,
            "submission_deadline": None,
            "estimated_value": None,
        }

    text = re.sub(r"\s+", " ", text).strip()

    title = None
    authority = None
    deadline = None
    estimated_value = None

    # Tender title
    title_patterns = [
        r"(?:tender\s+title|title\s+of\s+tender)"
        r"\s*[:\-]\s*(.+?)(?=\s+(?:tendering\s+authority|issuing\s+authority|"
        r"procuring\s+entity|estimated\s+(?:tender\s+)?value|"
        r"bid\s+submission\s+deadline|submission\s+deadline|closing\s+date))",

        r"(?:tender\s+for|procurement\s+of)\s+(.{5,200}?)"
        r"(?:\s+issued\s+by|\s+by\s+the\s+authority)",

        r"(?:e[-\s]?tender\s+for)\s+(.{5,200}?)(?=\s+"
        r"(?:tender\s+reference|tendering\s+authority|estimated\s+"
        r"(?:tender\s+)?value|bid\s+submission\s+deadline))",
    ]






    for pattern in title_patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            title = match.group(1).strip(" :-.,")
            break

    # Fallback for common government e-tender wording
    if not title:
        match = re.search(
            r"e[-\s]?tender\s+for\s+(.+?)(?=\.\s+)",
            text,
            re.IGNORECASE,
        )
        if match:
            title = match.group(1).strip(" :-.,")
    # Authority
    authority_patterns = [
        r"(?:tendering\s+authority|issuing\s+authority|procuring\s+entity)"
        r"\s*[:\-]\s*(.{3,150}?)(?:\s+deadline|\s+bid\s+submission|\s+estimated)",
        r"(?:issued\s+by|issued\s+from)\s*[:\-]?\s*(.{3,150}?)(?:\s+deadline|\s+bid\s+submission)",
    ]

    for pattern in authority_patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            authority = match.group(1).strip(" :-")
            break

    # Submission deadline
    deadline_patterns = [
        r"(?:submission\s+deadline|bid\s+submission\s+deadline|closing\s+date)"
        r"\s*[:\-]?\s*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}"
        r"(?:\s+\d{1,2}:\d{2}\s*(?:AM|PM)?)?)",

        r"(?:submission\s+deadline|bid\s+submission\s+deadline|closing\s+date)"
        r"\s*[:\-]?\s*([0-9]{1,2}\s+[A-Za-z]+\s+[0-9]{4}"
        r"(?:\s+\d{1,2}:\d{2}\s*(?:AM|PM)?)?)",
    ]

    for pattern in deadline_patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            deadline = match.group(1).strip()
            break

    # Estimated tender value
    value_patterns = [
        r"(?:estimated\s+(?:tender\s+)?value|tender\s+value)"
        r"\s*[:\-]?\s*((?:₹|Rs\.?|INR)\s*[\d,.]+\s*(?:crore|cr|lakh|lac)?)",

        r"(?:estimated\s+(?:tender\s+)?value|tender\s+value)"
        r"\s*[:\-]?\s*([\d,.]+\s*(?:crore|cr|lakh|lac))",
    ]

    for pattern in value_patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            estimated_value = match.group(1).strip()
            break

    return {
        "tender_title": title,
        "tendering_authority": authority,
        "submission_deadline": deadline,
        "estimated_value": estimated_value,
    }


def build_tender_summary(text: str) -> TenderSummary:
    """Build the complete structured Tender Summary."""

    metadata = extract_tender_metadata(text)
    requirements = extract_tender_requirements(text)

    all_requirements = (
        requirements["eligibility_requirements"]
        + requirements["financial_requirements"]
        + requirements["experience_requirements"]
        + requirements["mandatory_documents"]
        + requirements["special_conditions"]
    )

    summary_parts = []

    if metadata["tender_title"]:
        summary_parts.append(
            f"Tender: {metadata['tender_title']}"
        )

    if metadata["tendering_authority"]:
        summary_parts.append(
            f"Authority: {metadata['tendering_authority']}"
        )

    if metadata["submission_deadline"]:
        summary_parts.append(
            f"Submission deadline: {metadata['submission_deadline']}"
        )

    if metadata["estimated_value"]:
        summary_parts.append(
            f"Estimated value: {metadata['estimated_value']}"
        )

    if all_requirements:
        summary_parts.append(
            f"{len(all_requirements)} tender requirement(s) identified."
        )

    summary_text = " ".join(summary_parts)

    return TenderSummary(
        tender_title=metadata["tender_title"],
        tendering_authority=metadata["tendering_authority"],
        submission_deadline=metadata["submission_deadline"],
        estimated_value=metadata["estimated_value"],
        eligibility_requirements=requirements["eligibility_requirements"],
        financial_requirements=requirements["financial_requirements"],
        experience_requirements=requirements["experience_requirements"],
        mandatory_documents=requirements["mandatory_documents"],
        special_conditions=requirements["special_conditions"],
        attention_items=requirements["attention_items"],
        summary=summary_text,
    )

def tender_summary_to_dict(result: TenderSummary) -> dict:
    """Convert TenderSummary into backend/API-friendly JSON data."""

    return {
        "tender_title": result.tender_title,
        "tendering_authority": result.tendering_authority,
        "submission_deadline": result.submission_deadline,
        "estimated_value": result.estimated_value,
        "eligibility_requirements": [
            {
                "category": item.category,
                "requirement": item.requirement,
                "value": item.value,
                "mandatory": item.mandatory,
                "evidence_required": item.evidence_required,
            }
            for item in result.eligibility_requirements
        ],
        "financial_requirements": [
            {
                "category": item.category,
                "requirement": item.requirement,
                "value": item.value,
                "mandatory": item.mandatory,
                "evidence_required": item.evidence_required,
            }
            for item in result.financial_requirements
        ],
        "experience_requirements": [
            {
                "category": item.category,
                "requirement": item.requirement,
                "value": item.value,
                "mandatory": item.mandatory,
                "evidence_required": item.evidence_required,
            }
            for item in result.experience_requirements
        ],
        "mandatory_documents": [
            {
                "category": item.category,
                "requirement": item.requirement,
                "value": item.value,
                "mandatory": item.mandatory,
                "evidence_required": item.evidence_required,
            }
            for item in result.mandatory_documents
        ],
        "special_conditions": [
            {
                "category": item.category,
                "requirement": item.requirement,
                "value": item.value,
                "mandatory": item.mandatory,
                "evidence_required": item.evidence_required,
            }
            for item in result.special_conditions
        ],
        "attention_items": result.attention_items,
        "summary": result.summary,
    }


def tender_summary_to_json(result: TenderSummary) -> str:
    """Convert TenderSummary into a JSON string."""

    return json.dumps(
        tender_summary_to_dict(result),
        indent=2,
        ensure_ascii=False,
    )