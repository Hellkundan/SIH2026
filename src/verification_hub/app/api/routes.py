from collections import Counter

from fastapi import APIRouter, HTTPException

from app.models.bidder import BidderVerificationRequest
from app.models.enums import VerificationStatus, VerificationType
from app.models.request import BatchVerificationRequest, VerificationRequest
from app.models.response import (
    BatchVerificationResponse,
    BidderVerificationResponse,
    VerificationResponse,
)
from app.scenarios.demo_scenarios import SCENARIOS
from app.services.verification_service import VerificationService

router = APIRouter(prefix="/api/v1", tags=["Verification"])
service = VerificationService()


@router.post("/verification/verify", response_model=VerificationResponse)
async def verify(request: VerificationRequest):
    return await service.verify(request)


@router.post("/verification/verify-all", response_model=BatchVerificationResponse)
async def verify_all(request: BatchVerificationRequest):
    results = await service.verify_all(request.checks)
    return BatchVerificationResponse(results=results)


@router.post("/verification/bidder", response_model=BidderVerificationResponse)
async def verify_bidder(request: BidderVerificationRequest):
    checks = []
    field_to_type = {
        "pan": VerificationType.PAN,
        "gstin": VerificationType.GST,
        "udyam": VerificationType.UDYAM,
        "epfo": VerificationType.EPFO,
        "esic": VerificationType.ESIC,
        "startup_india": VerificationType.STARTUP_INDIA,
        "nsic": VerificationType.NSIC,
        "oem": VerificationType.OEM,
        "blacklist": VerificationType.BLACKLIST,
        "aadhaar": VerificationType.AADHAAR,
    }
    for field, vtype in field_to_type.items():
        identifier = getattr(request, field)
        if not identifier:
            continue
        checks.append(VerificationRequest(
            bidder_id=request.bidder_id,
            verification_type=vtype,
            identifier=identifier,
            company_name=request.company_name,
            pan=request.pan,
            gstin=request.gstin,
            aadhaar=request.aadhaar,
        ))

    # A blacklist/debarment check can use PAN as its identifier. This makes
    # the complete bidder endpoint automatically perform the adverse-record
    # check whenever a PAN is supplied, without requiring a second field.
    if request.pan and not request.blacklist:
        checks.append(VerificationRequest(
            bidder_id=request.bidder_id,
            verification_type=VerificationType.BLACKLIST,
            identifier=request.pan,
            company_name=request.company_name,
            pan=request.pan,
        ))

    if not checks:
        raise HTTPException(status_code=400, detail="At least one verification identifier is required")

    results = await service.verify_all(checks)
    counts = Counter(result.status.value for result in results)

    if any(r.status == VerificationStatus.MANUAL_REVIEW for r in results):
        overall = VerificationStatus.MANUAL_REVIEW
    elif any(r.status == VerificationStatus.FAILED for r in results):
        overall = VerificationStatus.FAILED
    elif any(r.status == VerificationStatus.NOT_FOUND for r in results):
        overall = VerificationStatus.NOT_FOUND
    elif any(r.status == VerificationStatus.PENDING for r in results):
        overall = VerificationStatus.PENDING
    else:
        overall = VerificationStatus.VERIFIED

    return BidderVerificationResponse(
        bidder_id=request.bidder_id,
        overall_status=overall,
        results=results,
        summary=dict(counts),
    )


@router.get("/providers")
async def providers():
    return {
        "mode": service.registry.mode,
        "providers": service.registry.provider_info(),
    }


@router.get("/scenarios/{scenario_name}", response_model=BatchVerificationResponse)
async def run_demo_scenario(scenario_name: str):
    factory = SCENARIOS.get(scenario_name.lower())
    if not factory:
        raise HTTPException(
            status_code=404,
            detail=f"Unknown scenario. Choose one of: {', '.join(SCENARIOS)}",
        )
    results = await service.verify_all(factory())
    return BatchVerificationResponse(results=results)
