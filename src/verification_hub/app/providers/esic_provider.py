from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockDatasetProvider

class ESICProvider(MockDatasetProvider):
    verification_type = VerificationType.ESIC
    dataset_file = "esic_data.json"
    identifier_keys = ('identifier',)
    name = "ESICProvider"
    source = "MOCK_ESIC_DATASET"

