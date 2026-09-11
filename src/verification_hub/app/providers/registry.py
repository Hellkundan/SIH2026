from app.core.config import settings
from app.models.enums import VerificationType
from app.providers.aadhaar_provider import AadhaarProvider
from app.providers.blacklist_provider import BlacklistProvider
from app.providers.epfo_provider import EPFOProvider
from app.providers.esic_provider import ESICProvider
from app.providers.gst_provider import GSTProvider
from app.providers.nsic_provider import NSICProvider
from app.providers.oem_provider import OEMProvider
from app.providers.pan_provider import PANProvider
from app.providers.start_provider import StartupIndiaProvider
from app.providers.udyam_provider import UdyamProvider


class ProviderRegistry:
    """Central provider registry. Service code never depends on provider implementation."""

    def __init__(self, mode: str | None = None):
        self.mode = (mode or settings.PROVIDER_MODE).lower()
        if self.mode != "mock":
            # Real adapters are intentionally not claimed to exist yet.
            # When added, only this factory needs to change.
            raise ValueError(
                f"Unsupported provider mode '{self.mode}'. "
                "Use PROVIDER_MODE=mock until real adapters are configured."
            )

        self.providers = {
            VerificationType.GST: GSTProvider(),
            VerificationType.UDYAM: UdyamProvider(),
            VerificationType.PAN: PANProvider(),
            VerificationType.AADHAAR: AadhaarProvider(),
            VerificationType.EPFO: EPFOProvider(),
            VerificationType.ESIC: ESICProvider(),
            VerificationType.STARTUP_INDIA: StartupIndiaProvider(),
            VerificationType.NSIC: NSICProvider(),
            VerificationType.OEM: OEMProvider(),
            VerificationType.BLACKLIST: BlacklistProvider(),
        }

    def get_provider(self, verification_type: VerificationType):
        return self.providers.get(verification_type)

    def available_types(self):
        return [item.value for item in self.providers]

    def provider_info(self):
        return {
            item.value: {
                "provider": provider.name,
                "source": provider.source,
                "mode": self.mode,
            }
            for item, provider in self.providers.items()
        }
