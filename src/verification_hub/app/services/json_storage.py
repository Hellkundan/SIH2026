import json
from pathlib import Path
from datetime import datetime

RESULTS_DIR = Path("verification_results")
RESULTS_DIR.mkdir(parents=True, exist_ok=True)


def save_verification_response(response):
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")

    filename = RESULTS_DIR / f"result_{timestamp}.json"

    if hasattr(response, "model_dump"):
        data = response.model_dump(mode="json")
    else:
        data = response

    with open(filename, "w", encoding="utf-8") as file:
        json.dump(data, file, indent=4, ensure_ascii=False)

    return filename