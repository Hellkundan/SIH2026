from abc import ABC, abstractmethod
from src.verification_hub.app.models.request import VerificationRequest
from src.verification_hub.app.models.response import VerificationResponse

class VerificationProvider(ABC):
    name : str = "BaseProvider"
    @abstractmethod
    async def verify(self, request: VerificationRequest) -> VerificationResponse:
        raise NotImplementedError