from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class PANProvider(MockDatasetProvider):
    verification_type = VerificationType.PAN
    dataset_file = "pan_data.json"
    identifier_keys = ('pan',)
    name = "PANProvider"
    source = "MOCK_PAN_DATASET"