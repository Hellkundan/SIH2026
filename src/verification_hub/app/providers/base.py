from abc import ABC, abstractmethod
from app.models.request import VerificationRequest
from app.models.response import VerificationResponse

class VerificationProvider(ABC):
    name : str = "BaseProvider"
    @abstractmethod
    async def verify(self, request: VerificationRequest) -> VerificationResponse:
        raise NotImplementedError