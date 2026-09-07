from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockProvider

class BlacklistProvider(MockProvider):
    verification_type: VerificationType.BLACKLIST
    dataset_file = "blacklist_dat.json"
    identifier_keys = ('pan', 'identifier')
    name = 'BlacklistProvider'
    source = "MOCK_BLACKLIST_DATASET"