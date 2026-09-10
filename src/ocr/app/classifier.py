"""
Step 4: Document type classifier.

A fast, explainable keyword/regex-based classifier. This is deliberately
NOT a black-box ML model — every classification comes with the matched
keywords as evidence, consistent with the project's "explainable, not
black-box" principle. Swap in a trained classifier later without
changing the output contract.
"""

import re
from typing import Tuple

DOCUMENT_SIGNATURES = {
    # \s* instead of \s+ between words: OCR frequently drops the space
    # between words (e.g. "INCOME TAX" -> "INCOMETAX"), so patterns must
    # tolerate zero-or-more whitespace, not require it.
    "GST": [r"\bgoods\s*and\s*services\s*tax\b", r"\bgstin\b", r"\bGST\b"],
    "PAN": [r"\bpermanent\s*account\s*number\b", r"\bincome\s*tax\s*department\b"],
    "UDYAM": [r"\budyam\b", r"\bmsme\b", r"\budyam\s*registration\b"],
    "EPFO": [r"\bemployees[\'\u2019]?\s*provident\s*fund\b", r"\bepfo\b", r"\bUAN\b"],
    "ESIC": [r"\bemployees[\'\u2019]?\s*state\s*insurance\b", r"\besic\b"],
    "OEM_AUTHORIZATION": [r"\boriginal\s*equipment\s*manufacturer\b", r"\bauthoriz(e|ation)\b.*\bdealer\b"],
    "STARTUP_INDIA": [r"\bstartup\s*india\b", r"\bdpiit\b"],
    "NSIC": [r"\bnsic\b", r"\bnational\s*small\s*industries\s*corporation\b"],
    "MAKE_IN_INDIA": [r"\bmake\s*in\s*india\b", r"\blocal\s*content\s*certificate\b"],
}


def classify_document(text: str) -> Tuple[str, float, list]:
    """
    Returns (document_type, confidence, matched_patterns).
    Confidence scales with number of distinct signatures matched.
    """
    lower_text = text.lower()
    best_type = "UNKNOWN"
    best_score = 0
    best_matches: list = []

    for doc_type, patterns in DOCUMENT_SIGNATURES.items():
        matches = [p for p in patterns if re.search(p, lower_text, re.IGNORECASE)]
        if len(matches) > best_score:
            best_score = len(matches)
            best_type = doc_type
            best_matches = matches

    if best_score == 0:
        return "UNKNOWN", 0.0, []

    # confidence: 1 match -> 0.6, 2 matches -> 0.85, 3+ -> 0.95
    confidence = min(0.6 + (best_score - 1) * 0.25, 0.95)
    return best_type, round(confidence, 2), best_matches
