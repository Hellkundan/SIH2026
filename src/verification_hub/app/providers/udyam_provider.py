from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class UdyamProvider(MockDatasetProvider):
    verification_type = VerificationType.UDYAM
    dataset_file = "udyam_data.json"
    identifier_keys = ('identifier',)
    name = "UdyamProvider"
    source = "MOCK_UDYAM_DATASET"