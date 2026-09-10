"""
Step 1: File ingestion & validation.

Accepts raw bytes + filename, checks type/size, and routes to the right
preprocessing path (PDF vs image).
"""

import hashlib
from pathlib import Path

ALLOWED_EXTENSIONS = {".pdf", ".jpg", ".jpeg", ".png"}
MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024  # 15 MB


class IngestionError(Exception):
    pass


def validate_file(filename: str, content: bytes) -> None:
    ext = Path(filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise IngestionError(
            f"Unsupported file type '{ext}'. Allowed: {sorted(ALLOWED_EXTENSIONS)}"
        )
    if len(content) == 0:
        raise IngestionError("Uploaded file is empty.")
    if len(content) > MAX_FILE_SIZE_BYTES:
        raise IngestionError(
            f"File exceeds max size of {MAX_FILE_SIZE_BYTES // (1024*1024)} MB."
        )


def compute_document_hash(content: bytes) -> str:
    """Used later for duplicate-document detection (Step 7)."""
    return hashlib.sha256(content).hexdigest()


def is_pdf(filename: str) -> bool:
    return Path(filename).suffix.lower() == ".pdf"
