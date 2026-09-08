from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class EPFOProvider(MockDatasetProvider):
    verification_type =  VerificationType.EPFO
    dataset_file = "epfo_data.json"
    identifier_keys = ('identifier',)
    name = "EPFOProvider"
    source = "MOCK_EPFO_DATASET"

