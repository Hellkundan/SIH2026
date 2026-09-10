import networkx as nx

from .schemas import Bidder, CollusionSignal


def build_collusion_graph(
    bidders,
    signals
):

    graph = nx.Graph()

    for bidder in bidders:

        graph.add_node(
            bidder.bidder_id,
            company_name=bidder.company_name
        )

    for signal in signals:

        if graph.has_edge(
            signal.bidder_a,
            signal.bidder_b
        ):

            edge = graph[
                signal.bidder_a
            ][
                signal.bidder_b
            ]

            edge["weight"] += signal.weight
            edge["signals"].append(
                signal.signal_type
            )

        else:

            graph.add_edge(
                signal.bidder_a,
                signal.bidder_b,
                weight=signal.weight,
                signals=[
                    signal.signal_type
                ]
            )

    return graph


def detect_clusters(graph):

    clusters = []

    components = nx.connected_components(
        graph
    )

    for index, component in enumerate(
        components,
        start=1
    ):

        bidder_ids = sorted(
            list(component)
        )

        if len(bidder_ids) < 2:
            continue

        clusters.append({
            "cluster_id": f"CR-{index:03d}",
            "bidder_ids": bidder_ids
        })

    return clusters