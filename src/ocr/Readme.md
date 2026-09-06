# DIXY — Document Intelligence Service (Member 3: OCR & Document Intelligence)

Turns uploaded bidder documents (PDF/JPG/PNG) into structured, confidence-scored
data for the Compliance Engine (Member 2) and Backend (Member 1) to consume.

## What's implemented (maps to your roadmap Steps 1–8)

| Step | File | What it does |
|---|---|---|
| 1 | `app/ingestion.py` | Validates file type/size, computes doc hash |
| 2 | `app/preprocessing.py` | PDF→image conversion, denoise/threshold, blur scoring |
| 3 | `app/ocr_engine.py` | Tesseract OCR + per-word confidence |
| 4 | `app/classifier.py` | Keyword/regex-based document type classifier (explainable) |
| 5 | `app/extractors.py` | Regex field extraction: PAN, GSTIN, Udyam, UAN |
| 6 | `app/quality.py` | Flags blurry/low-confidence/missing-field documents |
| 7 | `app/ingestion.py` | SHA-256 document hash (duplicate detection input for Member 2) |
| 8 | `app/main.py` | FastAPI endpoint: `POST /api/ocr/process` |

`app/pipeline.py` wires all 8 steps into one `process_document()` call.

## Setup

```bash
cd src/ocr
python3 -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirements.txt
```

You also need the **Tesseract binary** installed (this is separate from the
`pytesseract` Python package):

- Ubuntu/Debian: `sudo apt install tesseract-ocr`
- macOS: `brew install tesseract`
- Windows: install from https://github.com/UB-Mannheim/tesseract/wiki and add
  it to PATH (or set `pytesseract.pytesseract.tesseract_cmd` in `ocr_engine.py`)

## Run it

```bash
uvicorn app.main:app --reload --port 8001
```

Test with a sample document:

```bash
curl -X POST http://localhost:8001/api/ocr/process \
  -F "file=@/path/to/sample_gst_certificate.pdf"
```

You'll get back JSON matching `DocumentIntelligenceResult` in `app/schemas.py` —
this is the exact contract Member 1's backend and Member 2's AI service should
consume (see Section 6, "Integration Contract", in the team roadmap doc).

## What to do next (in priority order)

1. **Swap in real sample documents.** Get 4–5 real (or realistic dummy) GST,
   PAN, Udyam, EPFO certificates. Run them through `/api/ocr/process` and check
   whether `classifier.py`'s keyword patterns actually match your samples —
   government document wording varies, so you'll likely need to add a few more
   patterns per type.
2. **Tune `extractors.py` against real formats.** The regexes are the *correct
   official formats*, but double check GSTIN/Udyam number formatting against
   your actual sample docs — OCR misreads (0/O, 1/I) are common, so consider
   adding a light normalization step (e.g., replace common misreads) before
   matching.
3. **Add EPFO/ESIC/OEM/Startup India/NSIC classifier patterns** — right now
   only a starter set of keywords exists per type; expand `DOCUMENT_SIGNATURES`
   in `classifier.py` as you get real samples.
4. **Wire it into Member 1's orchestration** — once your endpoint is stable,
   share the OpenAPI schema (`/docs` when the server is running) with Member 1
   so they can call it from the Spring Boot backend.
5. **Hand your output schema to Member 2** — they consume `extracted_fields` +
   `raw_text` + `confidence` directly for entity matching.

## Testing

A quick sanity test without spinning up the server:

```bash
python3 -c "
from app.pipeline import process_document
with open('sample.pdf', 'rb') as f:
    result = process_document('sample.pdf', f.read())
print(result.model_dump_json(indent=2))
"
```
