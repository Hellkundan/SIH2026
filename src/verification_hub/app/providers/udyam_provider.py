from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockProvider

class UdyamProvider(MockDatasetProvider):
    verification_type = VerificationType.UDYAM
    dataset_file = "udyam_data.json"
    identifier_keys = ('identifier',)
    name = "UdyamProvider"
    source = "MOCK_UDYAM_DATASET"