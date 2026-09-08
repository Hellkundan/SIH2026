from datetime import datetime, timezone
from app.models.enums import ErrorState, VerificationStatus, VerificationType
from app.models.request import VerificationRequest
from app.models.response import VerificationEvidence, VerificationResponse
from app.providers.base import VerificationProvider
from app.utils.loader import load_json

class MockDatasetProvider(VerificationProvider):
    verification_type: VerificationType
    dataset_file: str
    identifier_keys: tuple[str, ...] = ("identifier",)
    name = "MockDatasetProvider"
    source = "MOCK_DATASET"

    def _request_values(self, request: VerificationRequest) -> list[str]:
        values = []
        for key in self.identifier_keys:
            value = getattr(request, key, None)
            if value:
                values.append(str(value).strip().upper())
        if request.identifier:
            values.append(str(request.identifier).strip().upper())
        return list(dict.fromkeys(values))

    async def verify(self, request: VerificationRequest) -> VerificationResponse:
        values = self._request_values(request)
        records = load_json(self.dataset_file)

        if not values:
            return VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=self.verification_type,
                status=VerificationStatus.MANUAL_REVIEW,
                source=self.source,
                timestamp=datetime.now(timezone.utc),
                evidence=VerificationEvidence(
                    provider=self.name,
                    source=self.source,
                    details={"reason": "No usable identifier supplied"}
                ),
                error_state=ErrorState.INVALID_REQUEST,
                error_message="Identifier required for this verification",
                confidence=0.0
            )

        for record in records:
            record_values = [
                str(record.get(key, "")).strip().upper()
                for key in self.identifier_keys + ("identifier",)
            ]
            if any(value and value in record_values for value in values):
                status = VerificationStatus.VERIFIED
                if record.get("record_status") == "NOT_FOUND":
                    status = VerificationStatus.NOT_FOUND

                matched = next(
                    (value for value in values if value in record_values),
                    values[0]
                )

                return VerificationResponse(
                    bidder_id=request.bidder_id,
                    verification_type=self.verification_type,
                    status=status,
                    source=self.source,
                    timestamp=datetime.now(timezone.utc),
                    evidence=VerificationEvidence(
                        provider=self.name,
                        source=self.source,
                        matched_identifier=matched,
                        details=record
                    ),
                    error_state=ErrorState.NONE,
                    confidence=0.95
                )

        return VerificationResponse(
            bidder_id=request.bidder_id,
            verification_type=self.verification_type,
            status=VerificationStatus.NOT_FOUND,
            source=self.source,
            timestamp=datetime.now(timezone.utc),
            evidence=VerificationEvidence(
                provider=self.name,
                source=self.source,
                matched_identifier=values[0],
                details={}
            ),
            error_state=ErrorState.NONE,
            confidence=0.9
        )
