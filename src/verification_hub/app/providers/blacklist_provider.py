from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockDatasetProvider

class BlacklistProvider(MockDatasetProvider):
    verification_type: VerificationType.BLACKLIST
    dataset_file = "blacklist_data.json"
    identifier_keys = ('pan', 'identifier')
    name = 'BlacklistProvider'
    source = "MOCK_BLACKLIST_DATASET"