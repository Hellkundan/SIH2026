import asyncio

import pytest

from app.core.config import settings
from app.models.enums import ErrorState, VerificationStatus, VerificationType
from app.models.request import VerificationRequest
from app.providers.base import VerificationProvider
from app.models.response import VerificationEvidence, VerificationResponse
from app.services.verification_service import VerificationService


class FailingProvider(VerificationProvider):
    name = "FailingProvider"
    source = "TEST"

    async def verify(self, request):
        raise ConnectionError("temporary failure")


class SlowProvider(VerificationProvider):
    name = "SlowProvider"
    source = "TEST"

    async def verify(self, request):
        await asyncio.sleep(settings.PROVIDER_TIMEOUT_SECONDS + 0.2)
        raise AssertionError("should have timed out")


class FlakyProvider(VerificationProvider):
    name = "FlakyProvider"
    source = "TEST"
    calls = 0

    async def verify(self, request):
        self.calls += 1
        if self.calls == 1:
            raise ConnectionError("first call fails")
        now = __import__("datetime").datetime.now(__import__("datetime").timezone.utc)
        return VerificationResponse(
            bidder_id=request.bidder_id,
            verification_type=request.verification_type,
            status=VerificationStatus.VERIFIED,
            source=self.source,
            timestamp=now,
            evidence=VerificationEvidence(
                provider=self.name, source=self.source, checked_at=now
            ),
        )


@pytest.mark.asyncio
async def test_provider_failure_is_normalized(monkeypatch):
    service = VerificationService()
    service.registry.providers[VerificationType.PAN] = FailingProvider()
    response = await service.verify(VerificationRequest(
        bidder_id="TEST-1", verification_type=VerificationType.PAN, identifier="ABCDE1234F"
    ))
    assert response.status == VerificationStatus.FAILED
    assert response.error_state == ErrorState.PROVIDER_UNAVAILABLE
    assert response.attempts == settings.RETRY_ATTEMPTS + 1


@pytest.mark.asyncio
async def test_provider_timeout_is_normalized(monkeypatch):
    monkeypatch.setattr(settings, "PROVIDER_TIMEOUT_SECONDS", 0.05)
    service = VerificationService()
    service.registry.providers[VerificationType.PAN] = SlowProvider()
    response = await service.verify(VerificationRequest(
        bidder_id="TEST-2", verification_type=VerificationType.PAN, identifier="ABCDE1234F"
    ))
    assert response.status == VerificationStatus.FAILED
    assert response.error_state == ErrorState.TIMEOUT


@pytest.mark.asyncio
async def test_retry_then_success(monkeypatch):
    monkeypatch.setattr(settings, "RETRY_BACKOFF_SECONDS", 0)
    provider = FlakyProvider()
    service = VerificationService()
    service.registry.providers[VerificationType.PAN] = provider
    response = await service.verify(VerificationRequest(
        bidder_id="TEST-3", verification_type=VerificationType.PAN, identifier="ABCDE1234F"
    ))
    assert response.status == VerificationStatus.VERIFIED
    assert response.attempts == 2

@pytest.mark.asyncio
async def test_all_required_demo_providers_are_registered():
    service = VerificationService()
    expected = {
        "PAN", "GST", "UDYAM", "EPFO", "ESIC",
        "STARTUP_INDIA", "NSIC", "OEM", "BLACKLIST"
    }
    assert expected.issubset(set(service.registry.available_types()))

@pytest.mark.asyncio
async def test_clean_scenario_has_nine_successful_checks():
    from app.scenarios.demo_scenarios import clean_bidder
    results = await VerificationService().verify_all(clean_bidder())
    assert len(results) == 9
    assert all(r.status == VerificationStatus.VERIFIED for r in results)

@pytest.mark.asyncio
async def test_suspicious_scenario_flags_adverse_records():
    from app.scenarios.demo_scenarios import suspicious_bidder
    results = await VerificationService().verify_all(suspicious_bidder())
    assert any(r.status == VerificationStatus.MANUAL_REVIEW for r in results)
