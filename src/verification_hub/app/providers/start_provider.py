from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockProvider

class StartupIndiaProvider(MockDatasetProvider):
    verification_type = VerificationType.STARTUP_INDIA
    dataset_file = "startup_india_data.json"
    identifier_keys = ('identifier',)
    name = "StartupIndiaProvider"
    source = "MOCK_STARTUP_INDIA_DATASET"