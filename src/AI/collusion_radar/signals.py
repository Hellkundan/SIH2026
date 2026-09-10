from typing import List

from .schemas import Bidder, CollusionSignal


def normalize(value):
    if value is None:
        return ""

    return str(value).strip().upper()


def compare_signal(
    bidder_a: Bidder,
    bidder_b: Bidder,
    field_name: str,
    signal_type: str,
    weight: int,
    description: str
):
    value_a = normalize(getattr(bidder_a, field_name, None))
    value_b = normalize(getattr(bidder_b, field_name, None))

    if not value_a or not value_b:
        return None

    if value_a != value_b:
        return None

    return CollusionSignal(
        bidder_a=bidder_a.bidder_id,
        bidder_b=bidder_b.bidder_id,
        signal_type=signal_type,
        value=value_a,
        weight=weight,
        explanation=description
    )


def detect_shared_signals(
    bidder_a: Bidder,
    bidder_b: Bidder
) -> List[CollusionSignal]:

    signals = []

    checks = [
        (
            "pan",
            "SHARED_PAN",
            100,
            "Both bidders share the same PAN."
        ),
        (
            "gstin",
            "SHARED_GSTIN",
            100,
            "Both bidders share the same GSTIN."
        ),
        (
            "udyam",
            "SHARED_UDYAM",
            80,
            "Both bidders share the same Udyam registration."
        ),
        (
            "address",
            "SHARED_ADDRESS",
            40,
            "Both bidders have the same registered address."
        ),
        (
            "phone",
            "SHARED_PHONE",
            50,
            "Both bidders share the same phone number."
        ),
        (
            "email",
            "SHARED_EMAIL",
            50,
            "Both bidders share the same email address."
        ),
        (
            "authorized_person",
            "SHARED_AUTHORIZED_PERSON",
            60,
            "Both bidders identify the same authorized person."
        ),
        (
            "bank_account",
            "SHARED_BANK_ACCOUNT",
            100,
            "Both bidders share the same bank account."
        )
    ]

    for (
        field_name,
        signal_type,
        weight,
        description
    ) in checks:

        signal = compare_signal(
            bidder_a,
            bidder_b,
            field_name,
            signal_type,
            weight,
            description
        )

        if signal:
            signals.append(signal)

    # Document reuse
    hashes_a = set(bidder_a.document_hashes)
    hashes_b = set(bidder_b.document_hashes)

    shared_hashes = hashes_a.intersection(hashes_b)

    for document_hash in shared_hashes:

        signals.append(
            CollusionSignal(
                bidder_a=bidder_a.bidder_id,
                bidder_b=bidder_b.bidder_id,
                signal_type="SHARED_DOCUMENT_HASH",
                value=document_hash,
                weight=80,
                explanation=(
                    "Both bidders submitted documents with "
                    "the same SHA-256 hash."
                )
            )
        )

    return signals