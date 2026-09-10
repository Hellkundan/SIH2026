from typing import List

from .schemas import CollusionSignal


SIGNAL_TO_FLAG = {
    "SHARED_PAN": "SHARED_PAN",
    "SHARED_GSTIN": "SHARED_GSTIN",
    "SHARED_UDYAM": "SHARED_UDYAM",
    "SHARED_ADDRESS": "SHARED_ADDRESS",
    "SHARED_PHONE": "SHARED_PHONE",
    "SHARED_EMAIL": "SHARED_EMAIL",
    "SHARED_AUTHORIZED_PERSON": "SHARED_DIRECTOR",
    "SHARED_BANK_ACCOUNT": "SHARED_BANK_ACCOUNT",
    "SHARED_DOCUMENT_HASH": "DUPLICATE_TEMPLATE",
}


def extract_pattern_flags(
    signals: List[CollusionSignal]
) -> List[str]:

    flags = []

    for signal in signals:

        flag = SIGNAL_TO_FLAG.get(
            signal.signal_type
        )

        if flag and flag not in flags:
            flags.append(flag)

    return flags