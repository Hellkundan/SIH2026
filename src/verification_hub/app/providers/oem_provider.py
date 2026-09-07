from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockProvider

class OEMProvider(MockDatasetProvider):
    verification_type = VerificationType.OEM
    dataset_file = "oem_data.json"
    identifier_keys = ('identifier',)
    name = "OEMProvider"
    source = "MOCK_OEM_DATASET"