from .signals import detect_shared_signals
from .scoring import calculate_collusion_score
from .patterns import extract_pattern_flags


def generate_explanation(
    bidder_a,
    bidder_b,
    signals,
    pattern_flags
):

    if not signals:
        return (
            f"No significant shared signals were detected "
            f"between {bidder_a.company_name} and "
            f"{bidder_b.company_name}."
        )

    explanations = []

    for signal in signals:
        explanations.append(
            signal.explanation
        )

    flag_text = ", ".join(pattern_flags)

    explanation = (
        f"{bidder_a.company_name} and "
        f"{bidder_b.company_name} share "
        f"{len(signals)} potentially relevant linkage "
        f"signal(s). Pattern flags: {flag_text}. "
        f"These signals warrant procurement-officer "
        f"review and do not by themselves establish "
        f"collusion or wrongdoing."
    )

    return explanation


def analyze_bidder_pair(
    bidder_a,
    bidder_b
):

    signals = detect_shared_signals(
        bidder_a,
        bidder_b
    )

    score, risk_level = calculate_collusion_score(
        signals
    )

    pattern_flags = extract_pattern_flags(
        signals
    )

    if risk_level == "CRITICAL":
        recommendation = "HIGH_RISK_CARTEL"

    elif risk_level in ("HIGH", "MEDIUM"):
        recommendation = "FLAG_FOR_REVIEW"

    else:
        recommendation = "NO_ACTION"

    explanation = generate_explanation(
        bidder_a,
        bidder_b,
        signals,
        pattern_flags
    )

    return {
        "bidder_ids": [
            bidder_a.bidder_id,
            bidder_b.bidder_id
        ],
        "score": score,
        "risk_level": risk_level,
        "connection_strength": min(
            100,
            round(score / 5, 2)
        ),
        "shared_signals": [
            {
                "type": signal.signal_type,
                "value": signal.value,
                "weight": signal.weight,
                "explanation": signal.explanation
            }
            for signal in signals
        ],
        "pattern_flags": pattern_flags,
        "explanation": explanation,
        "recommendation": recommendation
    }