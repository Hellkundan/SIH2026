import networkx as nx

from .demo_data import create_demo_bidders
from .signals import detect_shared_signals
from .graph import (
    build_collusion_graph,
    detect_clusters
)
from .cluster import analyze_cluster


def main():

    bidders = create_demo_bidders()

    print("=" * 70)
    print("DIXY COLLUSION RADAR - FULL TEST")
    print("=" * 70)

    all_signals = []

    # -----------------------------------------------------
    # PAIRWISE TEST
    # -----------------------------------------------------

    print("\nPAIRWISE ANALYSIS")
    print("-" * 70)

    for i in range(len(bidders)):

        for j in range(i + 1, len(bidders)):

            bidder_a = bidders[i]
            bidder_b = bidders[j]

            signals = detect_shared_signals(
                bidder_a,
                bidder_b
            )

            all_signals.extend(signals)

            print()
            print(
                f"{bidder_a.bidder_id} "
                f"<-> "
                f"{bidder_b.bidder_id}"
            )

            print(
                f"{bidder_a.company_name} "
                f"<-> "
                f"{bidder_b.company_name}"
            )

            print(
                "Shared signals:",
                len(signals)
            )

            if signals:

                total_score = sum(
                    signal.weight
                    for signal in signals
                )

                print(
                    "Score:",
                    total_score
                )

                for signal in signals:

                    print(
                        "  -",
                        signal.signal_type,
                        "(",
                        signal.weight,
                        ")"
                    )

            else:

                print(
                    "No shared signals"
                )

    # -----------------------------------------------------
    # GRAPH
    # -----------------------------------------------------

    graph = build_collusion_graph(
        bidders,
        all_signals
    )

    print()
    print("=" * 70)
    print("GRAPH")
    print("=" * 70)

    print(
        "Bidders:",
        graph.number_of_nodes()
    )

    print(
        "Connections:",
        graph.number_of_edges()
    )

    # -----------------------------------------------------
    # CLUSTERS
    # -----------------------------------------------------

    clusters = detect_clusters(
        graph
    )

    print()
    print("=" * 70)
    print("COLLUSION CLUSTERS")
    print("=" * 70)

    for cluster in clusters:

        result = analyze_cluster(
            cluster["cluster_id"],
            cluster["bidder_ids"],
            graph,
            all_signals
        )

        print()
        print(
            "Cluster:",
            result["cluster_id"]
        )

        print(
            "Bidders:",
            ", ".join(
                result["bidder_ids"]
            )
        )

        print(
            "Connection strength:",
            result["connection_strength"]
        )

        print(
            "Risk:",
            result["risk_level"]
        )

        print(
            "Pattern flags:",
            ", ".join(
                result["pattern_flags"]
            )
        )

        print(
            "Recommendation:",
            result["recommendation"]
        )

        print(
            "Explanation:"
        )

        print(
            result["explanation"]
        )

    # -----------------------------------------------------
    # CONNECTED COMPONENTS
    # -----------------------------------------------------

    print()
    print("=" * 70)
    print("NETWORK SUMMARY")
    print("=" * 70)

    print(
        "Connected components:",
        nx.number_connected_components(
            graph
        )
    )

    for index, component in enumerate(
        nx.connected_components(graph),
        start=1
    ):

        print(
            f"Group {index}:",
            ", ".join(
                sorted(component)
            )
        )


if __name__ == "__main__":
    main()