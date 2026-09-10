from app.models.enums import VerificationType
from app.providers.gst_provider import GSTProvider
from app.providers.udyam_provider import UdyamProvider
from app.providers.pan_provider import PANProvider
from app.providers.aadhaar_provider import AadhaarProvider
from app.providers.epfo_provider import EPFOProvider
from app.providers.esic_provider import ESICProvider
from app.providers.start_provider import StartupIndiaProvider
from app.providers.nsic_provider import NSICProvider
from app.providers.oem_provider import OEMProvider
from app.providers.blacklist_provider import BlacklistProvider


class ProviderRegistry:
    def __init__(self):
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
        return [item.value for item in self.providers.keys()]
