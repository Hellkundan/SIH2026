from abc import ABC, abstractmethod
from src.verification_hub.models.request import VerificationRequest
from src.verification_hub.models.response import VerificationResponse

class VerificationProvider(ABS):
    name : str = "BaseProvider"
    @abstractmethod
    async def verify(self, request: VerificationRequest) -> VerificationResponse:
        raise NotImplementedError