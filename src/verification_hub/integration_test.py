import json
from pathlib import Path

import requests


# ============================================================
# CONFIGURATION
# ============================================================

AI_URL = "http://127.0.0.1:8003/api/ai/analyze"

BASE_DIR = Path(__file__).resolve().parent
VERIFICATION_DIR = BASE_DIR / "verification"


# ============================================================
# LOAD ONE RESULT FROM EACH VERIFICATION TYPE
# ============================================================

VERIFICATION_TYPES = [
    "PAN",
    "GST",
    "UDYAM",
    "EPFO",
    "ESIC",
    "STARTUP_INDIA",
    "NSIC",
    "OEM",
    "BLACKLIST",
]


def load_first_result(verification_type):

    folder = VERIFICATION_DIR / verification_type

    files = sorted(folder.glob("result_*.json"))

    if not files:
        return None

    with open(files[0], "r", encoding="utf-8") as f:
        return json.load(f)


# ============================================================
# BUILD BIDDER DATA
# ============================================================

def build_bidder_data(results):

    bidder_data = {}

    for verification_type, result in results.items():

        if not result:
            continue

        evidence = result.get("evidence", {})
        details = evidence.get("details", {})

        if verification_type == "PAN":

            bidder_data["pan"] = {
                "number": details.get("pan"),
                "name": details.get("company_name"),
            }

        elif verification_type == "GST":

            bidder_data["gst"] = {
                "gstin": details.get("gstin"),
                "name": details.get("company_name"),
                "state": details.get("state"),
                "address": details.get("registered_address"),
            }

        elif verification_type == "UDYAM":

            bidder_data["udyam"] = {
                "number": details.get("identifier"),
                "name": details.get("company_name"),
                "pan": details.get("pan"),
            }

        elif verification_type == "EPFO":

            bidder_data["epfo"] = {
                "identifier": details.get("identifier"),
                "name": details.get("company_name"),
                "pan": details.get("pan"),
            }

        elif verification_type == "ESIC":

            bidder_data["esic"] = {
                "identifier": details.get("identifier"),
                "name": details.get("company_name"),
                "pan": details.get("pan"),
            }

        elif verification_type == "STARTUP_INDIA":

            bidder_data["startup_india"] = {
                "identifier": details.get("identifier"),
                "name": details.get("company_name"),
                "pan": details.get("pan"),
            }

        elif verification_type == "NSIC":

            bidder_data["nsic"] = {
                "identifier": details.get("identifier"),
                "name": details.get("company_name"),
                "pan": details.get("pan"),
            }

        elif verification_type == "OEM":

            bidder_data["oem"] = {
                "identifier": details.get("identifier"),
                "name": details.get("company_name"),
                "pan": details.get("pan"),
            }

        elif verification_type == "BLACKLIST":

            bidder_data["blacklist"] = {
                "identifier": details.get("identifier"),
                "name": details.get("company_name"),
                "pan": details.get("pan"),
            }

    return bidder_data


# ============================================================
# MAIN
# ============================================================

def main():

    print("=" * 70)
    print("DIXY VERIFICATION HUB → AI ENGINE INTEGRATION TEST")
    print("=" * 70)

    results = {}

    for verification_type in VERIFICATION_TYPES:

        result = load_first_result(verification_type)

        if result:

            results[verification_type] = result

            print(
                f"{verification_type:20} "
                f"{result.get('status', 'UNKNOWN'):20}"
            )

        else:

            print(
                f"{verification_type:20} "
                f"NO RESULT"
            )

    print("-" * 70)

    bidder_data = build_bidder_data(results)

    payload = {
        "bidder_data": bidder_data,
        "verification_results": results,
    }

    print("Sending combined results to AI...")
    print()

    try:

        response = requests.post(
            AI_URL,
            json=payload,
            timeout=30,
        )

        print("HTTP STATUS:", response.status_code)
        print()

        try:

            output = response.json()

            print(
                json.dumps(
                    output,
                    indent=2
                )
            )

        except Exception:

            print(response.text)

    except requests.exceptions.ConnectionError:

        print(
            "ERROR: Could not connect to AI Engine."
        )

        print(
            "Make sure AI is running on port 8003."
        )

    except Exception as e:

        print(
            "ERROR:",
            str(e)
        )


if __name__ == "__main__":
    main()

