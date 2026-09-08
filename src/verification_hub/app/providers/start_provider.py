from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class StartupIndiaProvider(MockDatasetProvider):
    verification_type = VerificationType.STARTUP_INDIA
    dataset_file = "startup_india_data.json"
    identifier_keys = ('identifier',)
    name = "StartupIndiaProvider"
    source = "MOCK_STARTUP_INDIA_DATASET"