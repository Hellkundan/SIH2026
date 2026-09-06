"""
Step 3: OCR baseline.

Wraps pytesseract. Returns both plain text and per-word confidence so we
can compute an overall OCR confidence for the document.
"""

from typing import Tuple
import pytesseract
pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
from PIL import Image


def run_ocr(image: Image.Image) -> Tuple[str, float]:
    """
    Returns (extracted_text, average_word_confidence 0-1).
    """
    data = pytesseract.image_to_data(image, output_type=pytesseract.Output.DICT)

    words = []
    confidences = []
    for i, word in enumerate(data["text"]):
        conf = data["conf"][i]
        # tesseract uses -1 for non-text regions
        if word.strip() and str(conf) != "-1":
            words.append(word)
            try:
                confidences.append(float(conf))
            except ValueError:
                pass

    text = " ".join(words)
    avg_conf = (sum(confidences) / len(confidences) / 100.0) if confidences else 0.0
    return text, round(avg_conf, 3)
