
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict, Any

from .dixy_ai_engine import dixy_analyze
from .tender_summary.summarizer import (
    build_tender_summary,
    tender_summary_to_dict,
)


app = FastAPI(
    title="DIXY AI Service",
    version="DIXY-AI-v0.1",
    description="AI-powered bidder identity consistency and compliance intelligence service"
)


class AIAnalyzeRequest(BaseModel):
    bidder_data: Dict[str, Any]
    verification_results: Optional[Dict[str, Any]] = None


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