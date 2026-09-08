from app.models.enums import VerificationType
from app.providers.mock.mock_base import MockDatasetProvider

class GSTProvider(MockDatasetProvider):
    verification_type = VerificationType.GST
    dataset_file = "gst_data.json"
    identifier_keys = ('gstin',)
    name = "GSTProvider"
    source = "MOCK_GST_DATASET"