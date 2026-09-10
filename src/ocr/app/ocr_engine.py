"""
Step 3: OCR baseline.

Wraps pytesseract. Returns both plain text and per-word confidence so we
can compute an overall OCR confidence for the document. 
"""

from typing import Tuple
from typing import Tuple
import pytesseract
from PIL import Image


def run_ocr(image: Image.Image, lang: str = "eng+hin") -> Tuple[str, float]:
    """
    Returns (extracted_text, average_word_confidence 0-1).

    lang="eng+hin" tells Tesseract to recognize BOTH English and Hindi
    (Devanagari) script in the same pass, instead of forcing Hindi text
    into English letter shapes (which produces gibberish). Requires
    hin.traineddata to be present in Tesseract's tessdata folder.
    Add more languages the same way, e.g. "eng+hin+tam" for Tamil.
    """
    data = pytesseract.image_to_data(image, lang=lang, output_type=pytesseract.Output.DICT)

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
