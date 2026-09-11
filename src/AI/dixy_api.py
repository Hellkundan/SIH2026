import json
from typing import Optional, Dict, Any, List
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

try:
    from dixy_ai_engine import dixy_analyze
    from tender_summary.summarizer import (
        build_tender_summary,
        tender_summary_to_dict,
    )
    from collusion_radar.schemas import Bidder
    from collusion_radar.signals import detect_shared_signals
    from collusion_radar.graph import build_collusion_graph, detect_clusters
    from collusion_radar.cluster import analyze_cluster
except ImportError:
    from .dixy_ai_engine import dixy_analyze
    from .tender_summary.summarizer import (
        build_tender_summary,
        tender_summary_to_dict,
    )
    from .collusion_radar.schemas import Bidder
    from .collusion_radar.signals import detect_shared_signals
    from .collusion_radar.graph import build_collusion_graph, detect_clusters
    from .collusion_radar.cluster import analyze_cluster


app = FastAPI(
    title="DIXY AI Service",
    version="DIXY-AI-v0.1",
    description="AI-powered bidder identity consistency and compliance intelligence service"
)


class AIAnalyzeRequest(BaseModel):
    bidder_data: Dict[str, Any]
    verification_results: Optional[Dict[str, Any]] = None


class CollusionAnalyzeRequest(BaseModel):
    tender_id: str
    bidders: List[Dict[str, Any]]


@app.get("/")
def root():
    return {
        "service": "DIXY AI",
        "version": "DIXY-AI-v0.1",
        "status": "running"
    }


@app.get("/health")
def health():
    return {
        "status": "healthy",
        "service": "dixy-ai",
        "version": "DIXY-AI-v0.1"
    }


@app.post("/api/ai/analyze")
def analyze_bidder(request: AIAnalyzeRequest):
    try:
        result = dixy_analyze(
            request.bidder_data,
            request.verification_results
        )
        return result
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"DIXY AI analysis failed: {str(e)}"
        )


@app.post("/api/ai/tender-summary")
def tender_summary(request: AIAnalyzeRequest):
    try:
        result = build_tender_summary(
            request.bidder_data.get("tender_text", "")
        )
        return tender_summary_to_dict(result)
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Tender Summary analysis failed: {str(e)}"
        )


@app.post("/api/collusion/analyze")
def analyze_collusion(request: CollusionAnalyzeRequest):
    try:
        if len(request.bidders) < 2:
            return {"clusters": []}

        bidders = [
            Bidder(
                bidder_id=b["bidder_id"],
                company_name=b.get("company_name", ""),
                pan=b.get("pan"),
                gstin=b.get("gstin"),
            )
            for b in request.bidders
        ]

        all_signals = []
        for i in range(len(bidders)):
            for j in range(i + 1, len(bidders)):
                all_signals.extend(detect_shared_signals(bidders[i], bidders[j]))

        graph = build_collusion_graph(bidders, all_signals)
        raw_clusters = detect_clusters(graph)

        clusters = []
        for c in raw_clusters:
            result = analyze_cluster(c["cluster_id"], c["bidder_ids"], graph, all_signals)
            if result is None:
                continue
            clusters.append({
                "cluster_id": result["cluster_id"],
                "bidder_ids": json.dumps(result["bidder_ids"]),
                "connection_strength": float(result["connection_strength"]),
                "shared_signals": json.dumps(result["shared_signals"]),
                "pattern_flags": json.dumps(result["pattern_flags"]),
                "explanation": result["explanation"],
                "recommendation": result["recommendation"],
            })

        return {"clusters": clusters}
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Collusion analysis failed: {str(e)}"
        )