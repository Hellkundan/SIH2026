import asyncio
import time
from datetime import datetime, timezone
from typing import Awaitable, Callable

from app.core.config import settings
from app.models.enums import ErrorState, VerificationStatus
from app.models.request import VerificationRequest
from app.models.response import VerificationEvidence, VerificationResponse
from app.providers.registry import ProviderRegistry
from app.services.json_storage import save_verification_response


class VerificationService:
    """Orchestrates provider calls with timeout, retry and failure isolation."""

    def __init__(self, registry: ProviderRegistry | None = None) -> None:
        self.registry = registry or ProviderRegistry()

    @staticmethod
    def _failure_response(
        request: VerificationRequest,
        provider,
        error_state: ErrorState,
        message: str,
        attempts: int,
        duration_ms: float,
    ) -> VerificationResponse:
        name = getattr(provider, "name", "Unknown") if provider else "Unknown"
        source = getattr(provider, "source", "NONE") if provider else "NONE"
        return VerificationResponse(
            bidder_id=request.bidder_id,
            verification_type=request.verification_type,
            status=VerificationStatus.FAILED,
            source=source,
            timestamp=datetime.now(timezone.utc),
            evidence=VerificationEvidence(
                provider=name,
                source=source,
                checked_at=datetime.now(timezone.utc),
                evidence_type="PROVIDER_ERROR",
                details={
                    "error_state": error_state.value,
                    "attempts": attempts,
                },
            ),
            error_state=error_state,
            error_message=message,
            confidence=0.0,
            attempts=attempts,
            duration_ms=round(duration_ms, 2),
        )

    async def _call_with_timeout(self, provider, request: VerificationRequest):
        return await asyncio.wait_for(
            provider.verify(request),
            timeout=settings.PROVIDER_TIMEOUT_SECONDS,
        )

    async def verify(self, request: VerificationRequest) -> VerificationResponse:
        provider = self.registry.get_provider(request.verification_type)
        started = time.perf_counter()

        if provider is None:
            response = self._failure_response(
                request, None, ErrorState.PROVIDER_UNAVAILABLE,
                f"No provider available for {request.verification_type.value}",
                0, (time.perf_counter() - started) * 1000,
            )
            save_verification_response(response)
            return response

        max_attempts = max(1, settings.RETRY_ATTEMPTS + 1)
        last_error = None

        for attempt in range(1, max_attempts + 1):
            try:
                response = await self._call_with_timeout(provider, request)
                response.attempts = attempt
                response.duration_ms = round((time.perf_counter() - started) * 1000, 2)
                save_verification_response(response)
                return response
            except asyncio.TimeoutError:
                last_error = ErrorState.TIMEOUT
                message = (
                    f"Provider timed out after {settings.PROVIDER_TIMEOUT_SECONDS}s "
                    f"(attempt {attempt}/{max_attempts})"
                )
            except (ConnectionError, TimeoutError, OSError) as exc:
                last_error = ErrorState.PROVIDER_UNAVAILABLE
                message = f"Provider unavailable: {exc} (attempt {attempt}/{max_attempts})"
            except Exception as exc:
                last_error = ErrorState.INTERNAL_ERROR
                message = f"Provider error: {exc} (attempt {attempt}/{max_attempts})"

            if attempt < max_attempts:
                await asyncio.sleep(settings.RETRY_BACKOFF_SECONDS * attempt)

        response = self._failure_response(
            request,
            provider,
            last_error or ErrorState.INTERNAL_ERROR,
            message,
            max_attempts,
            (time.perf_counter() - started) * 1000,
        )
        save_verification_response(response)
        return response

    async def verify_all(self, checks: list[VerificationRequest]) -> list[VerificationResponse]:
        # gather(..., return_exceptions=False) is safe because verify() isolates
        # every provider exception and always returns a normalized response.
        return list(await asyncio.gather(*(self.verify(request) for request in checks)))
