from dataclasses import asdict
import json

from .summarizer import (
    build_tender_summary,
    tender_summary_to_json,
)


SAMPLE_TENDER = """
TENDER TITLE: Supply of Industrial Equipment
TENDERING AUTHORITY: Department of Heavy Industries

Estimated Tender Value: Rs. 5 Crore

Bid Submission Deadline: 15/10/2026 17:00

The bidder must have valid GST registration.
Udyam/MSME registration is required.

The bidder must have minimum annual turnover of Rs. 5 Crore.

The bidder shall have minimum experience of 3 years
in supply of similar equipment.

The following documents are mandatory:
PAN document
GST certificate
Udyam certificate
Income Tax Return (ITR)
OEM authorization certificate

The bidder must provide Class-I local supplier
and Make in India declaration.
"""


def main():
    result = build_tender_summary(SAMPLE_TENDER)

    print("=" * 70)
    print("DIXY TENDER SUMMARY TEST")
    print("=" * 70)

    print(json.dumps(asdict(result), indent=2, ensure_ascii=False))

    print("\n" + "=" * 70)
    print("HUMAN-READABLE SUMMARY")
    print("=" * 70)
    print(result.summary)

    print("\n" + "=" * 70)
    print("REQUIREMENT COUNTS")
    print("=" * 70)
    print("Eligibility:", len(result.eligibility_requirements))
    print("Financial:", len(result.financial_requirements))
    print("Experience:", len(result.experience_requirements))
    print("Documents:", len(result.mandatory_documents))
    print("Special conditions:", len(result.special_conditions))

    print("\n" + "=" * 70)
    print("BACKEND JSON OUTPUT")
    print("=" * 70)
    print(tender_summary_to_json(result))


if __name__ == "__main__":
    main()