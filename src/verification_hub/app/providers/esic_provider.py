from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class ESICProvider(MockDatasetProvider):
    verification_type = VerificationType.ESIC
    dataset_file = "esic_data.json"
    identifier_keys = ('identifier',)
    name = "ESICProvider"
    source = "MOCK_ESIC_DATASET"

