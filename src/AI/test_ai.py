from pathlib import Path

p = Path("dixy_ai_engine.py")
s = p.read_text()

old = '''        if status in [
            "FAILED",
            "INVALID",
            "NOT_FOUND",
            "MISMATCH"
        ]:
'''

new = '''        if status in [
            "FAILED",
            "INVALID",
            "NOT_FOUND",
            "MISMATCH"
        ]:
'''

# Add MANUAL_REVIEW/PENDING handling immediately before VERIFIED.
marker = '''        elif status == "VERIFIED":
'''

replacement = '''        elif status == "MANUAL_REVIEW":
            
            findings.append({

                "field":
                    field,

                "status":
                    "MANUAL_REVIEW",

                "severity":
                    "HIGH",

                "reason":
                    result.get(
                        "reason",
                        "Verification requires human review."
                    ),

                "source":
                    result.get("source")

            })


        elif status == "PENDING":

            findings.append({

                "field":
                    field,

                "status":
                    "PENDING",

                "severity":
                    "MEDIUM",

                "reason":
                    result.get(
                        "reason",
                        "Verification is still pending."
                    ),

                "source":
                    result.get("source")

            })


        elif status == "VERIFIED":
'''

if marker not in s:
    raise SystemExit("ERROR: target code not found")

s = s.replace(marker, replacement, 1)
p.write_text(s)

print("FIX APPLIED")