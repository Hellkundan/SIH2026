from src.verification_hub.app.models.enums import VerificationType
from src.verification_hub.app.providers.gst_provider import GSTProvider
from src.verification_hub.app.providers.udyam_provider import UdyamProvider
from src.verification_hub.app.providers.pan_provider import PANProvider
from src.verification_hub.app.providers.epfo_provider import EPFOProvider
from src.verification_hub.app.providers.esic_provider import ESICProvider
from src.verification_hub.app.providers.startup_india_provider import StartupIndiaProvider
from src.verification_hub.app.providers.nsic_provider import NSICProvider
from src.verification_hub.app.providers.oem_provider import OEMProvider
from src.verification_hub.app.providers.blacklist_provider import BlacklistProvider


class ProviderRegistry:
    def __init__(self):
        self.providers = {
            VerificationType.GST: GSTProvider(),
            VerificationType.UDYAM: UdyamProvider(),
            VerificationType.PAN: PANProvider(),
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
        return [item.value for item in self.providers.keys()]
