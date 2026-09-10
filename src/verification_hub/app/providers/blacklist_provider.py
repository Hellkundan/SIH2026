from datetime import datetime, timezone

from app.models.enums import (
    ErrorState,
    VerificationStatus,
    VerificationType,
)
from app.models.request import VerificationRequest
from app.models.response import (
    VerificationEvidence,
    VerificationResponse,
)
from app.providers.mock.mock_base import MockDatasetProvider
from app.utils.matcher import find_match


class BlacklistProvider(MockDatasetProvider):
    """
    Specialized provider for blacklist/debarment checks.

    Blacklist semantics are intentionally different from normal
    registration/certificate verification:

        - No match:
            VERIFIED + finding=NO_MATCH

        - DEBARRED:
            MANUAL_REVIEW + finding=ADVERSE

        - UNDER_REVIEW:
            MANUAL_REVIEW + finding=UNDER_REVIEW

    VERIFIED here means only that no matching record was found in
    the consulted dataset. It does NOT mean legal clearance.
    """

    verification_type = VerificationType.BLACKLIST
    dataset_file = "blacklist_data.json"
    identifier_keys = ("pan", "identifier")

    name = "BlacklistProvider"
    source = "MOCK_BLACKLIST_DATASET"

    status_field = "debarment_status"

    async def verify(
        self,
        request: VerificationRequest,
    ) -> VerificationResponse:

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
                    details={
                        "finding": "INSUFFICIENT_EVIDENCE",
                        "reason": (
                            "No usable identifier or company "
                            "name supplied"
                        ),
                    },
                ),
                error_state=ErrorState.INVALID_REQUEST,
                error_message=(
                    "Identifier or company name required "
                    "for blacklist verification"
                ),
                confidence=0.0,
            )

        result = find_match(
            dataset_file=self.dataset_file,
            identifier_keys=self.identifier_keys,
            candidate_values=values,
            company_name=request.company_name,
        )

        # ---------------------------------------------------------
        # NO MATCH
        # ---------------------------------------------------------
        if result.record is None:
            return VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=self.verification_type,
                status=VerificationStatus.VERIFIED,
                source=self.source,
                timestamp=datetime.now(timezone.utc),
                evidence=VerificationEvidence(
                    provider=self.name,
                    source=self.source,
                    matched_identifier=None,
                    details={
                        "finding": "NO_MATCH",
                        "match_type": "none",
                        "searched_identifiers": values,
                        "clearance_scope": (
                            "No matching record found in "
                            "the consulted dataset"
                        ),
                    },
                ),
                error_state=ErrorState.NONE,
                confidence=result.confidence,
            )

        # ---------------------------------------------------------
        # MATCH FOUND
        # ---------------------------------------------------------
        record = result.record

        dataset_status = str(
            record.get(
                self.status_field,
                "",
            )
        ).strip().upper()

        details = dict(record)

        details["match_type"] = result.match_type
        details["searched_identifiers"] = values
        details["dataset_status"] = dataset_status

        if result.edit_distance >= 0:
            details["edit_distance"] = result.edit_distance

        if result.name_score is not None:
            details["name_similarity"] = round(
                result.name_score,
                2,
            )

        # ---------------------------------------------------------
        # DEBARRED
        # ---------------------------------------------------------
        if dataset_status == "DEBARRED":
            details["finding"] = "ADVERSE"
            details["requires_human_review"] = True

            return VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=self.verification_type,
                status=VerificationStatus.MANUAL_REVIEW,
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

        # ---------------------------------------------------------
        # UNDER REVIEW
        # ---------------------------------------------------------
        if dataset_status == "UNDER_REVIEW":
            details["finding"] = "UNDER_REVIEW"
            details["requires_human_review"] = True

            return VerificationResponse(
                bidder_id=request.bidder_id,
                verification_type=self.verification_type,
                status=VerificationStatus.MANUAL_REVIEW,
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

        # ---------------------------------------------------------
        # UNKNOWN BLACKLIST STATUS
        # ---------------------------------------------------------
        details["finding"] = "UNKNOWN_STATUS"
        details["requires_human_review"] = True

        return VerificationResponse(
            bidder_id=request.bidder_id,
            verification_type=self.verification_type,
            status=VerificationStatus.MANUAL_REVIEW,
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
