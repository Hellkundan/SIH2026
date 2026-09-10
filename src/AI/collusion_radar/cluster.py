from .scoring import calculate_collusion_score
from .patterns import extract_pattern_flags


def analyze_cluster(
    cluster_id,
    bidder_ids,
    graph,
    all_signals
):

    cluster_signals = [
        signal
        for signal in all_signals
        if (
            signal.bidder_a in bidder_ids
            and signal.bidder_b in bidder_ids
        )
    ]

    if not cluster_signals:
        return None

    pair_scores = []

    for bidder_a in bidder_ids:

        for bidder_b in bidder_ids:

            if bidder_a >= bidder_b:
                continue

            if graph.has_edge(
                bidder_a,
                bidder_b
            ):

                weight = graph[
                    bidder_a
                ][
                    bidder_b
                ]["weight"]

                pair_scores.append(
                    min(100, weight / 5)
                )

    if pair_scores:
        connection_strength = round(
            sum(pair_scores) / len(pair_scores),
            2
        )
    else:
        connection_strength = 0

    _, risk_level = calculate_collusion_score(
        cluster_signals
    )

    if connection_strength >= 80:
        recommendation = "HIGH_RISK_CARTEL"

    elif connection_strength >= 30:
        recommendation = "FLAG_FOR_REVIEW"

    else:
        recommendation = "NO_ACTION"

    pattern_flags = extract_pattern_flags(
        cluster_signals
    )

    signal_types = sorted(
        set(
            signal.signal_type
            for signal in cluster_signals
        )
    )

    explanation = (
        f"Cluster {cluster_id} contains "
        f"{len(bidder_ids)} connected bidders. "
        f"The network contains "
        f"{len(cluster_signals)} shared linkage signal(s), "
        f"including {', '.join(signal_types)}. "
        f"The observed connections warrant "
        f"procurement-officer review. "
        f"This is a decision-support signal and does not "
        f"establish collusion or wrongdoing."
    )

    return {
        "cluster_id": cluster_id,
        "bidder_ids": bidder_ids,
        "connection_strength": connection_strength,
        "shared_signals": [
            {
                "type": signal.signal_type,
                "value": signal.value,
                "weight": signal.weight,
                "explanation": signal.explanation
            }
            for signal in cluster_signals
        ],
        "pattern_flags": pattern_flags,
        "explanation": explanation,
        "recommendation": recommendation,
        "risk_level": risk_level
    }