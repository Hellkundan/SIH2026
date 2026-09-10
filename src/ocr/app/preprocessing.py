"""
Step 2: PDF/image preprocessing.

- Converts PDF pages to images (PyMuPDF, no poppler dependency needed).
- Applies basic OpenCV cleanup (grayscale, denoise, threshold) so OCR
  gets cleaner input.
- Computes a simple blur score used later as a quality signal.
"""

from io import BytesIO
from typing import List

import cv2
import numpy as np
from PIL import Image, ImageEnhance

MIN_OCR_WIDTH_PX = 1000  # images narrower than this get upscaled before OCR


def pdf_bytes_to_images(pdf_bytes: bytes, dpi: int = 200) -> List[Image.Image]:
    """Render every page of a PDF to a PIL Image."""
    import fitz  # PyMuPDF — imported lazily so image-only workflows don't need it

    images = []
    zoom = dpi / 72
    matrix = fitz.Matrix(zoom, zoom)
    with fitz.open(stream=pdf_bytes, filetype="pdf") as doc:
        for page in doc:
            pix = page.get_pixmap(matrix=matrix)
            img = Image.frombytes("RGB", (pix.width, pix.height), pix.samples)
            images.append(img)
    return images


def image_bytes_to_pil(image_bytes: bytes) -> Image.Image:
    return Image.open(BytesIO(image_bytes)).convert("RGB")


def upscale_if_small(image: Image.Image, min_width: int = MIN_OCR_WIDTH_PX) -> Image.Image:
    """
    Many real-world uploads (screenshots, cropped ID card photos) are tiny —
    a few hundred pixels wide — which is far below what OCR engines need to
    resolve small print like a PAN/GSTIN number. If the image is smaller
    than min_width, scale it up (preserving aspect ratio) with high-quality
    resampling before OCR runs. Leaves already-large images untouched.
    """
    width, height = image.size
    if width >= min_width:
        return image
    scale = min_width / width
    new_size = (int(width * scale), int(height * scale))
    upscaled = image.resize(new_size, Image.LANCZOS)
    return ImageEnhance.Sharpness(upscaled).enhance(2.0)


def clean_for_ocr(image: Image.Image) -> Image.Image:
    """Grayscale + denoise + adaptive threshold to improve OCR accuracy."""
    arr = np.array(image)
    gray = cv2.cvtColor(arr, cv2.COLOR_RGB2GRAY)
    denoised = cv2.fastNlMeansDenoising(gray, h=10)
    thresh = cv2.adaptiveThreshold(
        denoised, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 11
    )
    return Image.fromarray(thresh)


def blur_score(image: Image.Image) -> float:
    """
    Variance of the Laplacian — a standard, cheap blur/quality metric.
    Lower values mean blurrier images. Used as a quality signal (Step 6).
    """
    arr = np.array(image.convert("L"))
    return float(cv2.Laplacian(arr, cv2.CV_64F).var())
