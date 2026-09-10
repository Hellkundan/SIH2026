import json
from pathlib import Path

import requests


# --------------------------------------------------
# CONFIGURATION
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parent

DATA_DIR = BASE_DIR / "app" / "data"
VERIFICATION_DIR = BASE_DIR / "verification"

API_URL = "http://127.0.0.1:8002/api/v1/verification/verify"


# --------------------------------------------------
# DATASET CONFIGURATION
# --------------------------------------------------

DATASETS = {
    "gst_data.json": "GST",
    "pan_data.json": "PAN",
    "udyam_data.json": "UDYAM",
    "epfo_data.json": "EPFO",
    "esic_data.json": "ESIC",
    "startup_india_data.json": "STARTUP_INDIA",
    "nsic_data.json": "NSIC",
    "oem_data.json": "OEM",
    "blacklist_data.json": "BLACKLIST",
}


# --------------------------------------------------
# CREATE OUTPUT DIRECTORY
# --------------------------------------------------

VERIFICATION_DIR.mkdir(parents=True, exist_ok=True)


# --------------------------------------------------
# STATISTICS
# --------------------------------------------------

statistics = {
    "VERIFIED": 0,
    "NOT_FOUND": 0,
    "MANUAL_REVIEW": 0,
    "FAILED": 0,
    "PENDING": 0,
}

total_processed = 0


# --------------------------------------------------
# PROCESS ONE DATASET
# --------------------------------------------------

def process_dataset(filename, verification_type):

    global total_processed

    input_file = DATA_DIR / filename

    output_dir = VERIFICATION_DIR / verification_type
    output_dir.mkdir(parents=True, exist_ok=True)

    print()
    print("=" * 60)
    print(f"Processing: {filename}")
    print(f"Type:       {verification_type}")
    print("=" * 60)

    # Read JSON dataset
    with open(input_file, "r", encoding="utf-8") as file:
        records = json.load(file)

    print(f"Records found: {len(records)}")

    for index, record in enumerate(records, start=1):

        # --------------------------------------------------
        # GET IDENTIFIER
        # --------------------------------------------------

        if verification_type == "GST":
            identifier = record.get("gstin")

        else:
            identifier = record.get("identifier")

        company_name = record.get("company_name")
        pan = record.get("pan")

        # --------------------------------------------------
        # BUILD FASTAPI REQUEST
        # --------------------------------------------------

        request_data = {
            "bidder_id": f"{verification_type}-{index:03d}",
            "verification_type": verification_type,
            "identifier": identifier,
            "company_name": company_name,
            "pan": pan,
            "metadata": {
                "test_dataset": filename,
                "record_number": index
            }
        }

        try:

            # --------------------------------------------------
            # CALL FASTAPI
            # --------------------------------------------------

            response = requests.post(
                API_URL,
                json=request_data,
                timeout=10
            )

            # --------------------------------------------------
            # CHECK HTTP STATUS
            # --------------------------------------------------

            if response.status_code == 200:

                result = response.json()

                status = result.get(
                    "status",
                    "FAILED"
                )

                statistics[status] = (
                    statistics.get(status, 0) + 1
                )

                # --------------------------------------------------
                # SAVE VERIFICATION RESPONSE
                # --------------------------------------------------

                output_file = (
                    output_dir /
                    f"result_{index:03d}.json"
                )

                with open(
                    output_file,
                    "w",
                    encoding="utf-8"
                ) as file:

                    json.dump(
                        result,
                        file,
                        indent=4,
                        ensure_ascii=False
                    )

                print(
                    f"[{index:03d}/{len(records)}] "
                    f"{status} -> {output_file.name}"
                )

            else:

                print(
                    f"[{index:03d}/{len(records)}] "
                    f"HTTP ERROR {response.status_code}"
                )

                statistics["FAILED"] += 1

        except requests.exceptions.RequestException as exc:

            print(
                f"[{index:03d}/{len(records)}] "
                f"REQUEST ERROR: {exc}"
            )

            statistics["FAILED"] += 1

        total_processed += 1


# --------------------------------------------------
# MAIN
# --------------------------------------------------

def main():

    print()
    print("==============================================")
    print("     DIXY VERIFICATION HUB - BULK TEST")
    print("==============================================")

    print(f"Data directory:")
    print(DATA_DIR)

    print()
    print(f"Output directory:")
    print(VERIFICATION_DIR)

    print()
    print(f"FastAPI endpoint:")
    print(API_URL)

    # --------------------------------------------------
    # PROCESS ALL DATASETS
    # --------------------------------------------------

    for filename, verification_type in DATASETS.items():

        process_dataset(
            filename,
            verification_type
        )

    # --------------------------------------------------
    # FINAL SUMMARY
    # --------------------------------------------------

    print()
    print()
    print("==============================================")
    print("              FINAL SUMMARY")
    print("==============================================")

    print(
        f"Total processed: {total_processed}"
    )

    print()

    for status, count in statistics.items():

        print(
            f"{status:<20}: {count}"
        )

    print()
    print(
        f"Results saved in:\n{VERIFICATION_DIR}"
    )

    print()
    print("==============================================")


if __name__ == "__main__":
    main()