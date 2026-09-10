import asyncio
import json

from app.models.enums import VerificationType
from app.models.request import VerificationRequest
from app.providers.blacklist_provider import BlacklistProvider


async def main():
    provider = BlacklistProvider()

    data = json.load(
        open("app/data/blacklist_data.json")
    )

    print("\nBLACKLIST TARGETED TEST")
    print("=" * 70)

    for record in data:
        request = VerificationRequest(
            bidder_id="TEST",
            verification_type=VerificationType.BLACKLIST,
            identifier=record["identifier"],
            pan=record["pan"],
            company_name=record["company_name"],
        )

        result = await provider.verify(request)

        print(
            f'{record["company_name"]:<35} '
            f'{record["debarment_status"]:<15} -> '
            f'{result.status.value:<15} '
            f'finding={result.evidence.details.get("finding")}'
        )

    # Non-existent record
    request = VerificationRequest(
        bidder_id="TEST-NONE",
        verification_type=VerificationType.BLACKLIST,
        identifier="NONEXISTENT999",
        company_name="Definitely Not In Dataset",
    )

    result = await provider.verify(request)

    print("-" * 70)
    print(
        "NONEXISTENT RECORD ->",
        result.status.value,
        "finding=",
        result.evidence.details.get("finding"),
    )


asyncio.run(main())