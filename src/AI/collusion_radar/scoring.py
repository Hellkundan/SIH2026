def calculate_collusion_score(signals):

    score = sum(
        signal.weight
        for signal in signals
    )

    if score >= 150:
        level = "CRITICAL"

    elif score >= 100:
        level = "HIGH"

    elif score >= 50:
        level = "MEDIUM"

    else:
        level = "LOW"

    return score, level