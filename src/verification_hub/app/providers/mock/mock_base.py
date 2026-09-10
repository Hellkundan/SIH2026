from datetime import datetime, timezone

from app.models.enums import ErrorState, VerificationStatus, VerificationType
from app.models.request import VerificationRequest
from app.models.response import VerificationEvidence, VerificationResponse
from app.providers.base import VerificationProvider
from app.utils.matcher import find_match


class MockDatasetProvider(VerificationProvider):
    """
    Verifies a request's identifier(s) against a mock dataset using an
    indexed exact-match lookup with a bounded fuzzy fallback and
    name cross-check scoring (see app/utils/matcher.py).
    """

    verification_type: VerificationType
    dataset_file: str
    identifier_keys: tuple[str, ...] = ("identifier",)
    name = "MockDatasetProvider"
    source = "MOCK_DATASET"

    # Below this confidence, an "exact" ID match is treated as suspicious
    # (identifier matched but the supplied company name didn't) and routed
    # to manual review instead of being auto-verified.
    NAME_MISMATCH_CONFIDENCE_FLOOR = 0.5

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

        if not values and not request.company_name:
            return VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=self.verification_type,
                status=VerificationStatus.MANUAL_REVIEW,
                source=self.source,
                timestamp=datetime.now(timezone.utc),
                evidence=VerificationEvidence(
                    provider=self.name,
                    source=self.source,
                    details={"reason": "No usable identifier or company name supplied"},
                ),
                error_state=ErrorState.INVALID_REQUEST,
                error_message="Identifier or company name required for this verification",
                confidence=0.0,
            )

        result = find_match(
            dataset_file=self.dataset_file,
            identifier_keys=self.identifier_keys,
            candidate_values=values,
            company_name=request.company_name,
        )

        if result.record is None:
            return VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=self.verification_type,
                status=VerificationStatus.NOT_FOUND,
                source=self.source,
                timestamp=datetime.now(timezone.utc),
                evidence=VerificationEvidence(
                    provider=self.name,
                    source=self.source,
                    matched_identifier=result.matched_identifier,
                    details={"match_type": "none"},
                ),
                error_state=ErrorState.NONE,
                confidence=result.confidence,
            )

        record = result.record
        status = VerificationStatus.VERIFIED
        if record.get("record_status") == "NOT_FOUND":
            status = VerificationStatus.NOT_FOUND
        elif result.match_type == "fuzzy":
            # a typo-corrected match is real data, but shouldn't be silently
            # auto-accepted the same way an exact hit is
            status = VerificationStatus.MANUAL_REVIEW
        elif result.match_type == "exact" and result.confidence < self.NAME_MISMATCH_CONFIDENCE_FLOOR:
            # identifier matched, but the supplied company name doesn't —
            # possible identity mismatch, route for human review
            status = VerificationStatus.MANUAL_REVIEW
        elif result.match_type == "name_only":
            status = VerificationStatus.MANUAL_REVIEW

        details = dict(record)
        details["match_type"] = result.match_type
        if result.edit_distance >= 0:
            details["edit_distance"] = result.edit_distance
        if result.name_score is not None:
            details["name_similarity"] = round(result.name_score, 2)

        return VerificationResponse(
            bidder_id=request.bidder_id,
            verification_type=self.verification_type,
            status=status,
            source=self.source,
            timestamp=datetime.now(timezone.utc),
            evidence=VerificationEvidence(
                provider=self.name,
                source=self.source,
                matched_identifier=result.matched_identifier,
                details=details,
            ),
            error_state=ErrorState.NONE,
            confidence=result.confidence,
        )
