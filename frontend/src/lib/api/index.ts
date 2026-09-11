import type {
  ActivityItem,
  BidStatus,
  Bidder,
  ComplianceResult,
  DocumentIntelligence,
  DocumentType,
  Recommendation,
  Tender,
  TenderBid,
  TenderDocument,
  TenderRequirement,
  VerificationResult,
} from "../types";
import { USE_MOCK, delay, http } from "./client";
import * as store from "./mock-store";

/* ------------------------------------------------------------------ tenders */

export async function listTenders(): Promise<Tender[]> {
  if (!USE_MOCK) return http<Tender[]>("/tenders");
  await delay();
  return [...store.tenders];
}

export async function getTender(id: string): Promise<Tender> {
  if (!USE_MOCK) return http<Tender>(`/tenders/${id}`);
  await delay();
  const t = store.tenders.find((x) => x.id === id);
  if (!t) throw new Error("Tender not found");
  return t;
}

export async function createTender(input: Pick<Tender, "title" | "description">): Promise<Tender> {
  if (!USE_MOCK) return http<Tender>("/tenders", { method: "POST", body: JSON.stringify(input) });
  await delay();
  const tender: Tender = {
    id: store.nextId("TND"),
    status: "DRAFT",
    createdAt: store.nowIso(),
    ...input,
  };
  store.tenders.unshift(tender);
  return tender;
}

export async function updateTender(
  id: string,
  patch: Partial<Pick<Tender, "title" | "description" | "status">>,
): Promise<Tender> {
  if (!USE_MOCK)
    return http<Tender>(`/tenders/${id}`, { method: "PUT", body: JSON.stringify(patch) });
  await delay();
  const t = store.tenders.find((x) => x.id === id);
  if (!t) throw new Error("Tender not found");
  Object.assign(t, patch);
  return t;
}

export async function setTenderStatus(id: string, action: "open" | "close"): Promise<Tender> {
  if (!USE_MOCK) {
    const res = await http<Tender>(`/tenders/${id}/${action}`, { method: "POST" });
    return res ?? ({ id, status: action === "open" ? "OPEN" : "CLOSED" } as Tender);
  }
  await delay();
  return updateTender(id, { status: action === "open" ? "OPEN" : "CLOSED" });
}

/** Admin-only on the backend. */
export async function deleteTender(id: string): Promise<void> {
  if (!USE_MOCK) return http<void>(`/tenders/${id}`, { method: "DELETE" });
  await delay();
  const i = store.tenders.findIndex((t) => t.id === id);
  if (i >= 0) store.tenders.splice(i, 1);
}

/* ------------------------------------------------------------- requirements */

export async function listRequirements(tenderId: string): Promise<TenderRequirement[]> {
  if (!USE_MOCK) return http<TenderRequirement[]>(`/tender-requirements/tender/${tenderId}`);
  await delay();
  return store.requirements.filter((r) => r.tenderId === tenderId);
}

export async function saveRequirements(
  tenderId: string,
  next: Omit<TenderRequirement, "id">[],
): Promise<TenderRequirement[]> {
  if (!USE_MOCK)
    return http<TenderRequirement[]>("/tender-requirements", {
      method: "POST",
      body: JSON.stringify({ tenderId, requirements: next }),
    });
  await delay();
  for (let i = store.requirements.length - 1; i >= 0; i--) {
    if (store.requirements[i]!.tenderId === tenderId) store.requirements.splice(i, 1);
  }
  const saved = next.map((r) => ({ ...r, id: store.nextId("REQ") }));
  store.requirements.push(...saved);
  return saved;
}

/* ----------------------------------------------------------------- bidders */

export async function getBidder(id: string): Promise<Bidder> {
  if (!USE_MOCK) return http<Bidder>(`/bidders/${id}`);
  await delay();
  const b = store.bidders.find((x) => x.id === id);
  if (!b) throw new Error("Bidder not found");
  return b;
}

export async function listBidders(): Promise<Bidder[]> {
  if (!USE_MOCK) return http<Bidder[]>("/bidders");
  await delay();
  return [...store.bidders];
}

/**
 * Restricted to admin accounts on the backend — a public sign-up will 403.
 * The Register page surfaces that as a "pending review" message.
 */
export async function registerBidder(input: {
  companyName: string;
  email: string;
  phone: string;
}): Promise<Bidder> {
  if (!USE_MOCK) return http<Bidder>("/bidders", { method: "POST", body: JSON.stringify(input) });
  await delay(600);
  const bidder: Bidder = {
    id: store.nextId("BDR"),
    companyName: input.companyName,
    email: input.email,
    phone: input.phone,
    createdAt: store.nowIso(),
  };
  store.bidders.push(bidder);
  return bidder;
}

/** Admin-only on the backend. */
export async function deleteBidder(id: string): Promise<void> {
  if (!USE_MOCK) return http<void>(`/bidders/${id}`, { method: "DELETE" });
  await delay();
  const i = store.bidders.findIndex((b) => b.id === id);
  if (i >= 0) store.bidders.splice(i, 1);
}

/* --------------------------------------------------------------- documents */

export async function listDocumentsByBid(tenderBidId: string): Promise<TenderDocument[]> {
  if (!USE_MOCK) return http<TenderDocument[]>(`/documents/tender-bid/${tenderBidId}`);
  await delay();
  return store.documents.filter((d) => d.tenderBidId === tenderBidId);
}

export async function uploadDocument(input: {
  tenderBidId: string;
  documentType: DocumentType;
  fileName: string;
}): Promise<TenderDocument> {
  if (!USE_MOCK)
    return http<TenderDocument>("/documents", { method: "POST", body: JSON.stringify(input) });
  await delay(700);
  const doc: TenderDocument = {
    id: store.nextId("DOC"),
    tenderBidId: input.tenderBidId,
    documentType: input.documentType,
    fileName: input.fileName,
    status: "PROCESSING",
    uploadedAt: store.nowIso(),
  };
  store.documents.push(doc);
  return doc;
}

export async function deleteDocument(id: string): Promise<void> {
  if (!USE_MOCK) return http<void>(`/documents/${id}`, { method: "DELETE" });
  await delay(200);
  const i = store.documents.findIndex((d) => d.id === id);
  if (i >= 0) store.documents.splice(i, 1);
}

/* ------------------------------------ OCR + Verification Hub (separate) --- */

/**
 * OCR and registry results never travel on the Document row. They come from
 * the Document Intelligence / Verification Hub services. When no such service
 * is reachable we return null and the UI shows "verification pending" rather
 * than inventing a result.
 */
export async function getDocumentIntelligence(
  documentId: string,
): Promise<DocumentIntelligence | null> {
  if (!USE_MOCK) {
    try {
      return await http<DocumentIntelligence>(`/verification/document/${documentId}`);
    } catch {
      return null;
    }
  }
  await delay(240);
  return store.intelligence[documentId] ?? null;
}

/** Asks the pipeline to (re-)read and re-check a document. */
export async function runVerification(documentId: string): Promise<DocumentIntelligence | null> {
  if (!USE_MOCK) {
    try {
      return await http<DocumentIntelligence>(`/verification/document/${documentId}`, {
        method: "POST",
      });
    } catch {
      return null;
    }
  }
  await delay(1100);
  const doc = store.documents.find((d) => d.id === documentId);
  if (!doc) throw new Error("Document not found");
  const confidence = 0.78 + Math.random() * 0.2;
  const passed = confidence > 0.82;
  doc.status = passed ? "VERIFIED" : "PROCESSED";
  const result: DocumentIntelligence = {
    documentId,
    ocr: {
      documentNumber:
        store.intelligence[documentId]?.ocr?.documentNumber ?? `EXTRACTED-${documentId.slice(-4)}`,
      holderName: store.intelligence[documentId]?.ocr?.holderName ?? "Bidding entity",
      confidence: Number(confidence.toFixed(2)),
      quality: confidence > 0.85 ? "GOOD" : "LOW_CONFIDENCE",
    },
    verification: {
      type: doc.documentType,
      status: passed ? "VERIFIED" : "MANUAL_REVIEW",
      provider: "Verification Hub",
      errorState: null,
      checkedAt: store.nowIso(),
      message: passed ? undefined : "Extraction confidence below threshold — queued for review.",
    },
  };
  store.intelligence[documentId] = result;
  return result;
}

export async function getBlacklistCheck(bidderId: string): Promise<VerificationResult | null> {
  if (!USE_MOCK) {
    try {
      return await http<VerificationResult>(`/verification/blacklist/${bidderId}`);
    } catch {
      return null;
    }
  }
  await delay(200);
  return store.blacklistCheck(bidderId);
}

/* -------------------------------------------------------------------- bids */

export async function listBidsByBidder(bidderId: string): Promise<TenderBid[]> {
  if (!USE_MOCK) return http<TenderBid[]>(`/tender-bids/bidder/${bidderId}`);
  await delay();
  return store.bids.filter((b) => b.bidderId === bidderId);
}

export async function listBidsByTender(tenderId: string): Promise<TenderBid[]> {
  if (!USE_MOCK) return http<TenderBid[]>(`/tender-bids/tender/${tenderId}`);
  await delay();
  return store.bids.filter((b) => b.tenderId === tenderId);
}

export async function listAllBids(): Promise<TenderBid[]> {
  if (!USE_MOCK) return http<TenderBid[]>("/tender-bids");
  await delay();
  return [...store.bids];
}

export async function getBid(id: string): Promise<TenderBid> {
  if (!USE_MOCK) return http<TenderBid>(`/tender-bids/${id}`);
  await delay();
  const bid = store.bids.find((b) => b.id === id);
  if (!bid) throw new Error("Bid not found");
  return bid;
}

/** Documents need a bid to hang off, so the draft bid is created up front. */
export async function ensureDraftBid(tenderId: string, bidderId: string): Promise<TenderBid> {
  const mine = await listBidsByBidder(bidderId);
  const existing = mine.find((b) => b.tenderId === tenderId);
  if (existing) return existing;

  if (!USE_MOCK)
    return http<TenderBid>("/tender-bids", {
      method: "POST",
      body: JSON.stringify({ tenderId, bidderId, status: "DRAFT", quote: 0 }),
    });

  await delay(400);
  const id = store.nextId("BID");
  const bid: TenderBid = {
    id,
    reference: `DIXY/2026/${id}`,
    tenderId,
    bidderId,
    status: "DRAFT",
    quote: 0,
    updatedAt: store.nowIso(),
    history: [{ at: store.nowIso(), label: "Bid created as draft", by: "You" }],
  };
  store.bids.unshift(bid);
  return bid;
}

export async function saveBid(input: {
  id: string;
  tenderId: string;
  bidderId: string;
  quote: number;
  notes?: string | undefined;
  submit?: boolean | undefined;
}): Promise<TenderBid> {
  if (!USE_MOCK)
    return http<TenderBid>(`/tender-bids/${input.id}`, {
      method: "PUT",
      body: JSON.stringify(input),
    });
  await delay(500);
  const bid = store.bids.find((b) => b.id === input.id);
  if (!bid) throw new Error("Bid not found");
  bid.quote = input.quote;
  bid.notes = input.notes;
  bid.updatedAt = store.nowIso();
  if (input.submit) {
    bid.status = "SUBMITTED";
    bid.submittedAt = store.nowIso();
    bid.history.push({ at: store.nowIso(), label: "Bid submitted", by: "You" });
  }
  return bid;
}

export async function setBidStatus(
  id: string,
  action: "review" | "qualify" | "disqualify",
  remarks?: string,
): Promise<TenderBid> {
  if (!USE_MOCK)
    return http<TenderBid>(`/tender-bids/${id}/${action}`, {
      method: "POST",
      body: JSON.stringify({ remarks }),
    });
  await delay(500);
  const bid = store.bids.find((b) => b.id === id);
  if (!bid) throw new Error("Bid not found");
  const status: BidStatus =
    action === "review" ? "UNDER_REVIEW" : action === "qualify" ? "QUALIFIED" : "DISQUALIFIED";
  bid.status = status;
  bid.officerRemarks = remarks ?? bid.officerRemarks;
  bid.updatedAt = store.nowIso();
  bid.history.push({
    at: store.nowIso(),
    label:
      action === "review"
        ? "Marked under review"
        : action === "qualify"
          ? "Qualified"
          : "Disqualified",
    by: "You (Procurement Officer)",
  });
  return bid;
}

/* ---------------------------------------------------------------- activity */

export async function listActivity(): Promise<ActivityItem[]> {
  if (!USE_MOCK) return http<ActivityItem[]>("/activity");
  await delay();
  return [...store.activity];
}

export async function getComplianceResult(tenderBidId: string): Promise<ComplianceResult | null> {
  if (!USE_MOCK) {
    try {
      return await http<ComplianceResult>(`/tender-bids/${tenderBidId}/compliance`);
    } catch {
      return null;
    }
  }
  await delay(200);
  return null;
}

export async function getRecommendation(tenderBidId: string): Promise<Recommendation | null> {
  if (!USE_MOCK) {
    try {
      return await http<Recommendation>(`/tender-bids/${tenderBidId}/recommendation`);
    } catch {
      return null;
    }
  }
  await delay(200);
  return null;
}
