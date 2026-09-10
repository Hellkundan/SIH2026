from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class AadhaarProvider(MockDatasetProvider):
    verification_type = VerificationType.AADHAAR
    dataset_file = "aadhaar_data.json"
    identifier_keys = ('aadhaar', 'identifier')
    name = "AadhaarProvider"
    source = "MOCK_AADHAAR_DATASET"
