from pathlib import Path

p = Path("dixy_ai_engine.py")
s = p.read_text()

old = '''    failed_authoritative_verification = any(

        item["status"] in [
            "FAILED",
            "INVALID",
            "NOT_FOUND",
            "MISMATCH"
        ]

        for item in verification_analysis[
            "verification_findings"
        ]

    )
'''

new = '''    failed_authoritative_verification = any(

        item["status"] in [
            "FAILED",
            "INVALID",
            "NOT_FOUND",
            "MISMATCH",
            "MANUAL_REVIEW"
        ]

        for item in verification_analysis[
            "verification_findings"
        ]

    )
'''

if old not in s:
    raise SystemExit("TARGET NOT FOUND")

p.write_text(s.replace(old, new, 1))

print("FINAL DECISION FIXED")