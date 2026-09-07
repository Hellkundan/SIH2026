from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockProvider

from verification_hub.app.models.enums import VerificationType


class NSICProvider(MockDatasetProvider):
    verification_type: VerificationType.NSIC
    dataset_file = "nsic_data.json"
    identifier_keys = ('identifier',)
    name = "NSICProvider"
    source = "MOCK_NSIC_DATASET"
