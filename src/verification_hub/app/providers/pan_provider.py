from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockProvider

class PANProvider(MockDatasetProvider):
    verification_type = VerificationType.PAN
    dataset_file = "pan_data.json"
    identifier_keys = ('pan',)
    name = "PANProvider"
    source = "MOCK_PAN_DATASET"