from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class OEMProvider(MockDatasetProvider):
    verification_type = VerificationType.OEM
    dataset_file = "oem_data.json"
    identifier_keys = ('identifier',)
    status_field = "authorization_status"
    name = "OEMProvider"
    source = "MOCK_OEM_DATASET"