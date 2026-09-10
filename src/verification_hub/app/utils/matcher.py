"""
Indexed + fuzzy identifier matching engine for mock verification providers.

Replaces the old O(n) linear "scan every record, string-compare every field"
approach with:

  1. An O(1) hash-index lookup for exact identifier matches (built once per
     dataset and cached — not rebuilt on every request).
  2. A bounded edit-distance fuzzy fallback (only runs when the exact lookup
     misses), so a single-character typo in a PAN/GSTIN/Udyam number still
     resolves instead of silently returning NOT_FOUND. The fallback is
     length-bucketed so it never has to compare against the whole dataset.
  3. Real confidence scoring: exact ID match + matching company name scores
     highest; exact ID match with a mismatched name is flagged (possible
     identity mismatch) rather than blindly trusted; fuzzy matches are scored
     by how close the correction was.

Datasets here are small (tens to low hundreds of records), so the win isn't
about raw scale — it's about doing a real lookup instead of a linear scan,
and about the fuzzy/name-aware logic a plain `in` check can't do at all.
"""

from __future__ import annotations

import difflib
from dataclasses import dataclass
from functools import lru_cache
from typing import Any, Optional

from app.utils.loader import load_json

MAX_EDIT_DISTANCE = 2          # how many character edits we'll still call a "typo"
LENGTH_BUCKET_SLACK = 2        # only compare ids within +/- this many chars of the query


def _normalize(value: Any) -> str:
    return str(value).strip().upper() if value not in (None, "") else ""


def _normalize_name(value: Any) -> str:
    """Loose company-name normalization for cross-checking, not exact ID matching."""
    if not value:
        return ""
    text = str(value).upper().strip()
    for suffix in (" PVT LTD", " PRIVATE LIMITED", " LTD", " LIMITED", " LLP", " & CO", " CO"):
        if text.endswith(suffix):
            text = text[: -len(suffix)]
    return " ".join(text.split())


def _bounded_edit_distance(a: str, b: str, max_dist: int) -> int:
    """
    Levenshtein distance with an early-exit band: if the running minimum in a
    row already exceeds max_dist, bail out instead of finishing the DP table.
    Cheap on short identifiers (PAN/GSTIN/Udyam numbers are <= ~20 chars).
    """
    if abs(len(a) - len(b)) > max_dist:
        return max_dist + 1

    prev = list(range(len(b) + 1))
    for i, ca in enumerate(a, start=1):
        cur = [i] + [0] * len(b)
        row_min = cur[0]
        for j, cb in enumerate(b, start=1):
            cost = 0 if ca == cb else 1
            cur[j] = min(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
            row_min = min(row_min, cur[j])
        if row_min > max_dist:
            return max_dist + 1
        prev = cur
    return prev[-1]


@dataclass
class DatasetIndex:
    records: list[dict]
    exact_index: dict[str, dict]          # normalized identifier -> record
    by_length: dict[int, list[str]]       # len(id) -> [normalized ids] (fuzzy search buckets)
    name_index: dict[str, list[dict]]     # normalized company name -> [records]


@lru_cache(maxsize=None)
def _build_index(dataset_file: str, identifier_keys: tuple[str, ...]) -> DatasetIndex:
    records = load_json(dataset_file)
    exact_index: dict[str, dict] = {}
    by_length: dict[int, list[str]] = {}
    name_index: dict[str, list[dict]] = {}

    all_keys = identifier_keys + ("identifier",)
    for record in records:
        for key in all_keys:
            norm = _normalize(record.get(key))
            if norm:
                exact_index[norm] = record
                by_length.setdefault(len(norm), []).append(norm)

        name_norm = _normalize_name(record.get("company_name"))
        if name_norm:
            name_index.setdefault(name_norm, []).append(record)

    return DatasetIndex(records=records, exact_index=exact_index, by_length=by_length, name_index=name_index)


def clear_index_cache() -> None:
    _build_index.cache_clear()


def _fuzzy_lookup(index: DatasetIndex, query: str) -> Optional[tuple[dict, str, int]]:
    """Search only ids of similar length. Returns (record, matched_id, distance) or None."""
    best: Optional[tuple[dict, str, int]] = None
    for length in range(len(query) - LENGTH_BUCKET_SLACK, len(query) + LENGTH_BUCKET_SLACK + 1):
        for candidate_id in index.by_length.get(length, []):
            dist = _bounded_edit_distance(query, candidate_id, MAX_EDIT_DISTANCE)
            if dist <= MAX_EDIT_DISTANCE and (best is None or dist < best[2]):
                best = (index.exact_index[candidate_id], candidate_id, dist)
                if dist == 1:
                    return best  # good enough, stop early
    return best


@dataclass
class MatchResult:
    record: Optional[dict]
    matched_identifier: Optional[str]
    match_type: str          # "exact" | "fuzzy" | "none"
    edit_distance: int       # 0 for exact
    name_score: Optional[float]   # similarity ratio if a company_name was supplied, else None
    confidence: float


def find_match(
    dataset_file: str,
    identifier_keys: tuple[str, ...],
    candidate_values: list[str],
    company_name: Optional[str] = None,
) -> MatchResult:
    index = _build_index(dataset_file, identifier_keys)

    # ---- Pass 1: O(1) exact lookup for every candidate identifier ----
    for value in candidate_values:
        record = index.exact_index.get(value)
        if record:
            name_score = _score_name(record, company_name)
            confidence = _confidence_for_exact(name_score)
            return MatchResult(
                record=record,
                matched_identifier=value,
                match_type="exact",
                edit_distance=0,
                name_score=name_score,
                confidence=confidence,
            )

    # ---- Pass 2: bounded fuzzy fallback (only reached if no exact hit) ----
    best_fuzzy: Optional[tuple[dict, str, int]] = None
    for value in candidate_values:
        hit = _fuzzy_lookup(index, value)
        if hit and (best_fuzzy is None or hit[2] < best_fuzzy[2]):
            best_fuzzy = hit

    if best_fuzzy:
        record, matched_id, dist = best_fuzzy
        name_score = _score_name(record, company_name)
        confidence = _confidence_for_fuzzy(dist, name_score)
        return MatchResult(
            record=record,
            matched_identifier=matched_id,
            match_type="fuzzy",
            edit_distance=dist,
            name_score=name_score,
            confidence=confidence,
        )

    # ---- Pass 3: no identifier matched at all — try name-only fallback ----
    if company_name:
        name_norm = _normalize_name(company_name)
        candidates = index.name_index.get(name_norm)
        if not candidates:
            # fuzzy name match as a last resort (e.g. "Pvt Ltd" vs "Private Limited" drift,
            # or a minor spelling difference)
            close = difflib.get_close_matches(name_norm, index.name_index.keys(), n=1, cutoff=0.85)
            if close:
                candidates = index.name_index[close[0]]
        if candidates:
            record = candidates[0]
            return MatchResult(
                record=record,
                matched_identifier=None,
                match_type="name_only",
                edit_distance=-1,
                name_score=1.0,
                confidence=0.55,  # deliberately capped — identifier is the trustworthy key, name alone isn't
            )

    return MatchResult(
        record=None,
        matched_identifier=candidate_values[0] if candidate_values else None,
        match_type="none",
        edit_distance=-1,
        name_score=None,
        confidence=0.9,  # confident it's genuinely absent, since exact + fuzzy + name all missed
    )


def _score_name(record: dict, company_name: Optional[str]) -> Optional[float]:
    if not company_name:
        return None
    a = _normalize_name(record.get("company_name"))
    b = _normalize_name(company_name)
    if not a or not b:
        return None
    return difflib.SequenceMatcher(None, a, b).ratio()


def _confidence_for_exact(name_score: Optional[float]) -> float:
    if name_score is None:
        return 0.95  # ID matched, no name given to cross-check
    if name_score >= 0.9:
        return 0.99  # ID matched AND name matches closely
    if name_score >= 0.6:
        return 0.85  # ID matched, name loosely similar (formatting drift)
    return 0.4  # ID matched but the supplied name doesn't match the record — flag it


def _confidence_for_fuzzy(edit_distance: int, name_score: Optional[float]) -> float:
    base = {1: 0.75, 2: 0.6}.get(edit_distance, 0.5)
    if name_score is not None and name_score >= 0.9:
        base += 0.1
    return round(min(base, 0.9), 2)
