# import asyncio
# from datetime import datetime, timezone
#
# from app.models.enums import ErrorState, VerificationStatus
# from app.models.request import VerificationRequest
# from app.models.response import VerificationEvidence, VerificationResponse
# from app.providers.registry import ProviderRegistry
#
#
# class VerificationService:
#     def __init__(self) -> None:
#         self.registry = ProviderRegistry()
#
#     async def verify(self, request: VerificationRequest) -> VerificationResponse:
#         provider = self.registry.get_provider(request.verification_type)
#
#         if provider is None:
#             return VerificationResponse(
#                 bidder_id=request.bidder_id,
#                 verification_type=request.verification_type,
#                 status=VerificationStatus.FAILED,
#                 source="NONE",
#                 timestamp=datetime.now(timezone.utc),
#                 evidence=VerificationEvidence(
#                     provider="Unknown",
#                     source="NONE",
#                     details={"reason": "No provider registered for this verification type"},
#                 ),
#                 error_state=ErrorState.PROVIDER_UNAVAILABLE,
#                 error_message=f"No provider available for {request.verification_type}",
#                 confidence=0.0,
#             )
#
#         try:
#             return await provider.verify(request)
#         except Exception as exc:  # keep the API resilient during a live demo
#             return VerificationResponse(
#                 bidder_id=request.bidder_id,
#                 verification_type=request.verification_type,
#                 status=VerificationStatus.FAILED,
#                 source=getattr(provider, "source", "UNKNOWN"),
#                 timestamp=datetime.now(timezone.utc),
#                 evidence=VerificationEvidence(
#                     provider=getattr(provider, "name", "Unknown"),
#                     source=getattr(provider, "source", "UNKNOWN"),
#                     details={"error": str(exc)},
#                 ),
#                 error_state=ErrorState.INTERNAL_ERROR,
#                 error_message=str(exc),
#                 confidence=0.0,
#             )
#
#     async def verify_all(self, checks: list[VerificationRequest]) -> list[VerificationResponse]:
#         results = await asyncio.gather(*(self.verify(request) for request in checks))
#         return list(results)

import asyncio
from datetime import datetime, timezone

from app.models.enums import ErrorState, VerificationStatus
from app.models.request import VerificationRequest
from app.models.response import VerificationEvidence, VerificationResponse
from app.providers.registry import ProviderRegistry
from app.services.json_storage import save_verification_response


class VerificationService:
    def __init__(self) -> None:
        self.registry = ProviderRegistry()

    async def verify(
        self,
        request: VerificationRequest
    ) -> VerificationResponse:

        provider = self.registry.get_provider(
            request.verification_type
        )

        # ---------------------------------------------------------
        # CASE 1: No provider available
        # ---------------------------------------------------------
        if provider is None:

            response = VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=request.verification_type,
                status=VerificationStatus.FAILED,
                source="NONE",
                timestamp=datetime.now(timezone.utc),

                evidence=VerificationEvidence(
                    provider="Unknown",
                    source="NONE",
                    details={
                        "reason": (
                            "No provider registered for "
                            "this verification type"
                        )
                    },
                ),

                error_state=ErrorState.PROVIDER_UNAVAILABLE,
                error_message=(
                    f"No provider available for "
                    f"{request.verification_type}"
                ),
                confidence=0.0,
            )

            # Save failed response as JSON
            save_verification_response(response)

            return response

        # ---------------------------------------------------------
        # CASE 2: Provider verification
        # ---------------------------------------------------------
        try:

            response = await provider.verify(request)

            # Save successful/provider response as JSON
            save_verification_response(response)

            return response

        # ---------------------------------------------------------
        # CASE 3: Provider throws an exception
        # ---------------------------------------------------------
        except Exception as exc:

            response = VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=request.verification_type,
                status=VerificationStatus.FAILED,
                source=getattr(
                    provider,
                    "source",
                    "UNKNOWN"
                ),
                timestamp=datetime.now(timezone.utc),

                evidence=VerificationEvidence(
                    provider=getattr(
                        provider,
                        "name",
                        "Unknown"
                    ),
                    source=getattr(
                        provider,
                        "source",
                        "UNKNOWN"
                    ),
                    details={
                        "error": str(exc)
                    },
                ),

                error_state=ErrorState.INTERNAL_ERROR,
                error_message=str(exc),
                confidence=0.0,
            )

            # Save error response as JSON
            save_verification_response(response)

            return response

    async def verify_all(
        self,
        checks: list[VerificationRequest]
    ) -> list[VerificationResponse]:

        # Run all verification checks
        results = await asyncio.gather(
            *(self.verify(request) for request in checks)
        )

        return list(results)
