import json
import requests

with open("verification/PAN/result_001.json", "r") as f:
    pan_result = json.load(f)

details = pan_result["evidence"]["details"]

bidder_data = {
    "pan": {
        "number": details.get("pan"),
        "name": details.get("company_name")
    }
}

# IMPORTANT:
# AI expects each verification result directly under the field name.
verification_results = {
    "PAN": pan_result
}

payload = {
    "bidder_data": bidder_data,
    "verification_results": verification_results
}

print("Sending verification result to AI...")

response = requests.post(
    "http://127.0.0.1:8003/api/ai/analyze",
    json=payload,
    timeout=30
)

print("HTTP STATUS:", response.status_code)
print(json.dumps(response.json(), indent=2))