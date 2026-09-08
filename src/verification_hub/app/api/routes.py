from fastapi import APIRouter
from src.verification_hub.app.models.request import BatchVerificationRequest, VerificationRequest
from src.verification_hub.app.models.response import BatchVerificationResponse, VerificationResponse
# from app.services.scenario_service import (
#     clean_bidder_requests,
#     incomplete_bidder_requests,
#     suspicious_bidder_requests,
# )
from src.verification_hub.app.services.verification_service import VerificationService

router = APIRouter(prefix="/api/v1", tags=["Verification"])
service = VerificationService()


@router.post("/verification/verify", response_model=VerificationResponse)
async def verify(request: VerificationRequest):
    return await service.verify(request)


@router.post("/verification/verify-all", response_model=BatchVerificationResponse)
async def verify_all(request: BatchVerificationRequest):
    results = await service.verify_all(request.checks)
    return BatchVerificationResponse(results=results)


@router.get("/providers")
async def providers():
    return {"providers": service.registry.available_types()}

