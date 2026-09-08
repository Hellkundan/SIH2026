import asyncio
from datetime import datetime, timezone
from app.models.enums import VerificationStatus, ErrorState
from app.models.request import VerificationRequest
from app.models.response import VerificationEvidence, VerificationResponse
from app.providers.registry import ProviderRegistry

class VerificationService:
    def __init__(self):
        self.registry = ProviderRegistry()
        self.timeout_seconds = 5

    async def verify(self, request: VerificationRequest) -> VerificationResponse:
        provider = self.registry.get_provider(request.verification_type)

        if provider is None:
            return self._failure(
                request,
                VerificationStatus.FAILED,
                ErrorState.PROVIDER_UNAVAILABLE,
                "Provider is not registered"
            )

        try:
            return await asyncio.wait_for(
                provider.verify(request),
                timeout=self.timeout_seconds
            )
        except asyncio.TimeoutError:
            return self._failure(
                request,
                VerificationStatus.PENDING,
                ErrorState.TIMEOUT,
                "Provider timeout"
            )
        except Exception as exc:
            return self._failure(
                request,
                VerificationStatus.FAILED,
                ErrorState.INTERNAL_ERROR,
                str(exc)
            )

    async def verify_all(self, requests: list[VerificationRequest]):
        tasks = [self.verify(request) for request in requests]
        return await asyncio.gather(*tasks)

    def _failure(self, request, status, error_state, message):
        return VerificationResponse(
            bidder_id=request.bidder_id,
            verification_type=request.verification_type,
            status=status,
            source="VERIFICATION_HUB",
            timestamp=datetime.now(timezone.utc),
            evidence=VerificationEvidence(
                provider="VERIFICATION_HUB",
                source="VERIFICATION_HUB",
                details={}
            ),
            error_state=error_state,
            error_message=message,
            confidence=0.0
        )
