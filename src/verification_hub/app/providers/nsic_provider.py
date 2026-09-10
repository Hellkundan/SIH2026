from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class NSICProvider(MockDatasetProvider):
    verification_type = VerificationType.NSIC
    dataset_file = "nsic_data.json"
    identifier_keys = ('identifier',)
    status_field = "certificate_status"
    name = "NSICProvider"
    source = "MOCK_NSIC_DATASET"
