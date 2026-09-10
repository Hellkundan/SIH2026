"""
Step 8: Document Intelligence API.

Run locally:
    uvicorn app.main:app --reload --port 8001

Then POST a file to /api/ocr/process — this is the endpoint Member 1's
backend (and Member 2's AI service) will call.
"""

from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from .pipeline import process_document
from .schemas import DocumentIntelligenceResult

app = FastAPI(
    title="DIXY Document Intelligence Service",
    description="OCR, classification, and field extraction for bid compliance documents.",
    version="0.1.0",
)

# Allows the standalone UI in ui/index.html (opened directly as a local
# file) to call this API from the browser. Does not touch any OCR logic.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/ocr/process", response_model=DocumentIntelligenceResult)
async def process(file: UploadFile = File(...)):
    content = await file.read()
    result = process_document(file.filename, content)
    return JSONResponse(content=result.model_dump())
