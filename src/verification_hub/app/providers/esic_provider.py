from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockProvider

class ESICProvider(MockProvider):
    verification_type = VerificationType.ESIC
    dataset_file = "esc_data.json"
    identifier_keys = ('identifier',)
    name = "ESICProvider"
    source_file = "MOCK_ESIC_DATASET"

