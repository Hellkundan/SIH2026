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
from app.providers.base import VerificationProvider
from app.utils.matcher import find_match


class MockDatasetProvider(VerificationProvider):
    """
    Generic provider for mock verification datasets.

    Each provider defines:
        - verification_type
        - dataset_file
        - identifier_keys
        - status_field
        - name
        - source
    """

    verification_type: VerificationType
    dataset_file: str
    identifier_keys: tuple[str, ...] = ("identifier",)
    status_field: str = "status"

    name = "MockDatasetProvider"
    source = "MOCK_DATASET"

    NAME_MISMATCH_CONFIDENCE_FLOOR = 0.5

    VERIFIED_RECORD_STATUSES = {
        "ACTIVE",
        "VALID",
        "REGISTERED",
        "APPROVED",
        "COMPLIANT",
        "RECOGNIZED",
        "AUTHORIZED",
    }

    REVIEW_RECORD_STATUSES = {
        "SUSPENDED",
        "INACTIVE",
        "DEACTIVATED",
        "CANCELLED",
        "CANCELED",
        "EXITED",
        "BLACKLISTED",
        "DEBARRED",
        "UNDER_REVIEW",
        "EXPIRED",
        "REVOKED",
    }

    PENDING_RECORD_STATUSES = {
        "PENDING",
        "PENDING_VERIFICATION",
        "UNDER_PROCESS",
    }

    def _request_values(
        self,
        request: VerificationRequest,
    ) -> list[str]:
        """Collect usable identifiers from the request."""

        values = []

        for key in self.identifier_keys:
            value = getattr(request, key, None)

            if value:
                values.append(
                    str(value).strip().upper()
                )

        if request.identifier:
            values.append(
                str(request.identifier).strip().upper()
            )

        return list(dict.fromkeys(values))

    def _status_from_record(
        self,
        record: dict,
        match_type: str,
        confidence: float,
    ) -> VerificationStatus:
        """
        Convert dataset-specific status into DIXY's normalized status.
        """

        record_status = str(
            record.get("record_status", "")
        ).strip().upper()

        if record_status == "NOT_FOUND":
            return VerificationStatus.NOT_FOUND

        dataset_status = str(
            record.get(
                self.status_field,
                ""
            )
        ).strip().upper()

        # GST uses registration_status.
        if not dataset_status:
            dataset_status = str(
                record.get(
                    "registration_status",
                    ""
                )
            ).strip().upper()

        # PENDING states.
        if dataset_status in self.PENDING_RECORD_STATUSES:
            return VerificationStatus.PENDING

        # Adverse / review states.
        if dataset_status in self.REVIEW_RECORD_STATUSES:
            return VerificationStatus.MANUAL_REVIEW

        # Positive states.
        if dataset_status in self.VERIFIED_RECORD_STATUSES:

            # Fuzzy matches must always be reviewed.
            if match_type == "fuzzy":
                return VerificationStatus.MANUAL_REVIEW

            # Name-only matches must always be reviewed.
            if match_type == "name_only":
                return VerificationStatus.MANUAL_REVIEW

            # Exact identifier but strong name mismatch.
            if (
                match_type == "exact"
                and confidence < self.NAME_MISMATCH_CONFIDENCE_FLOOR
            ):
                return VerificationStatus.MANUAL_REVIEW

            return VerificationStatus.VERIFIED

        # Unknown source status should never silently become VERIFIED.
        return VerificationStatus.MANUAL_REVIEW

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
                        "reason": (
                            "No usable identifier or "
                            "company name supplied"
                        )
                    },
                ),
                error_state=ErrorState.INVALID_REQUEST,
                error_message=(
                    "Identifier or company name required "
                    "for this verification"
                ),
                confidence=0.0,
            )

        result = find_match(
            dataset_file=self.dataset_file,
            identifier_keys=self.identifier_keys,
            candidate_values=values,
            company_name=request.company_name,
        )

        # No matching record.
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
                    details={
                        "match_type": "none",
                        "searched_identifiers": values,
                    },
                ),
                error_state=ErrorState.NONE,
                confidence=result.confidence,
            )

        record = result.record

        status = self._status_from_record(
            record=record,
            match_type=result.match_type,
            confidence=result.confidence,
        )

        details = dict(record)

        details["match_type"] = result.match_type

        if result.edit_distance >= 0:
            details["edit_distance"] = result.edit_distance

        if result.name_score is not None:
            details["name_similarity"] = round(
                result.name_score,
                2,
            )

        details["searched_identifiers"] = values

        dataset_status = record.get(
            self.status_field
        )

        if dataset_status is None:
            dataset_status = record.get(
                "registration_status"
            )

        details["dataset_status"] = dataset_status

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