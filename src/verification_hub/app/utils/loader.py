import json
from functools import lru_cache
from pathlib import Path

DATA_DIR = Path(__file__).resolve().parent.parent / "data"


@lru_cache(maxsize=None)
def _read_file(filename: str) -> str:
    path = DATA_DIR / filename
    if not path.exists():
        raise FileNotFoundError(f"Mock dataset not found: {path}")
    return path.read_text(encoding="utf-8")


def load_json(filename: str) -> list[dict]:
    """
    Loads a mock dataset JSON file from app/data/.
    Cached in-memory so repeated verify calls don't hit disk every time.
    Call load_json.cache_clear() (see clear_cache below) if a dataset
    file is edited while the server is running.
    """
    raw = _read_file(filename)
    return json.loads(raw)


def clear_cache() -> None:
    _read_file.cache_clear()
