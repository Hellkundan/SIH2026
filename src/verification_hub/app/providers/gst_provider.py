from src.verfiation_hub.models.enums import VerificationType
from src.verification_hub.app.providers.mock.mock_base import MockDatasetProvider

class GSTProvider(MockDatasetProvider):
    verification_type = VerificationType.GST
    dataset_file = "gst_data.json"
    identifier_keys = ('gstin',)
    name = "GSTProvider"
    source = "MOCK_GST_DATASET"