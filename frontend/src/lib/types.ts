export const DOCUMENT_TYPES = [
  "PAN",
  "GST",
  "UDYAM",
  "EPFO",
  "ESIC",
  "STARTUP_INDIA",
  "NSIC",
  "OEM_AUTHORIZATION",
  "MAKE_IN_INDIA",
  "OTHER",
] as const;
export type DocumentType = (typeof DOCUMENT_TYPES)[number];

export const DOCUMENT_LABELS: Record<DocumentType, string> = {
  PAN: "PAN Card",
  GST: "GST Registration",
  UDYAM: "Udyam / MSME Certificate",
  EPFO: "EPFO Registration",
  ESIC: "ESIC Registration",
  STARTUP_INDIA: "Startup India Recognition",
  NSIC: "NSIC Registration",
  OEM_AUTHORIZATION: "OEM Authorization Letter",
  MAKE_IN_INDIA: "Make in India Declaration",
  OTHER: "Other Document",
};

export const documentLabel = (type: string) => DOCUMENT_LABELS[type as DocumentType] ?? type;

export type TenderStatus = "DRAFT" | "OPEN" | "CLOSED";
export type BidStatus = "DRAFT" | "SUBMITTED" | "UNDER_REVIEW" | "QUALIFIED" | "DISQUALIFIED";
export type DocumentStatus = "UPLOADED" | "PROCESSING" | "PROCESSED" | "VERIFIED" | "FAILED";
export type VerificationStatus = "VERIFIED" | "NOT_FOUND" | "FAILED" | "PENDING" | "MANUAL_REVIEW";
export type VerificationErrorState = "PROVIDER_UNAVAILABLE" | "TIMEOUT" | "INVALID_REQUEST" | null;

/** The three roles the Spring Boot backend issues. */
export type Role = "BIDDER" | "PROCUREMENT_OFFICER" | "ADMIN";

/** Backend Tender: title, description, status, createdAt — nothing else. */
export interface Tender {
  id: string;
  title: string;
  description: string;
  status: TenderStatus;
  createdAt: string;
}

/** Backend Bidder: companyName, email, phone, createdAt. */
export interface Bidder {
  id: string;
  companyName: string;
  email: string;
  phone: string;
  createdAt: string;
}

/** Backend TenderRequirement: a free-text sentence, mandatory or not. */
export interface TenderRequirement {
  id: string;
  tenderId: string;
  requirement: string;
  mandatory: boolean;
}

/** Backend Document: always belongs to a TenderBid, never to a bidder. */
export interface TenderDocument {
  id: string;
  tenderBidId: string;
  documentType: DocumentType;
  fileName: string;
  status: DocumentStatus;
  uploadedAt: string;
}

export interface OcrResult {
  documentNumber: string;
  holderName: string;
  issuedOn?: string | undefined;
  validTill?: string | undefined;
  confidence: number;
  quality: "GOOD" | "LOW_CONFIDENCE" | "BLURRY";
}

export interface VerificationResult {
  type: string;
  status: VerificationStatus;
  provider: string;
  errorState: VerificationErrorState;
  checkedAt: string;
  message?: string | undefined;
}

/**
 * OCR + registry data never travels on the Document row — it comes from the
 * Document Intelligence and Verification Hub services, keyed by document id.
 */
export interface DocumentIntelligence {
  documentId: string;
  ocr?: OcrResult | undefined;
  verification?: VerificationResult | undefined;
}

export interface BidEvent {
  at: string;
  label: string;
  by: string;
}

export interface TenderBid {
  id: string;
  reference: string;
  tenderId: string;
  bidderId: string;
  status: BidStatus;
  quote: number;
  notes?: string | undefined;
  submittedAt?: string | undefined;
  updatedAt: string;
  officerRemarks?: string | undefined;
  history: BidEvent[];
}

export interface ActivityItem {
  at: string;
  label: string;
}

/** What /auth/login actually returns, plus the token. */
export interface AuthUser {
  username: string;
  role: Role;
  token: string;
}

/** Chosen locally by the bidder, because login never returns a bidderId. */
export interface BidderProfile {
  id: string;
  companyName: string;
}

export const isOfficerRole = (role: Role | undefined) =>
  role === "PROCUREMENT_OFFICER" || role === "ADMIN";
export const isAdminRole = (role: Role | undefined) => role === "ADMIN";

export const ROLE_LABELS: Record<Role, string> = {
  BIDDER: "Bidder",
  PROCUREMENT_OFFICER: "Procurement Officer",
  ADMIN: "Administrator",
};

export interface ComplianceResult {
  id: string;
  tenderBidId: string;
  entityMatchScore: number;
  severity: string;
  discrepancies: string;
  explanation: string;
  evaluatedAt: string;
}

export interface Recommendation {
  id: string;
  tenderBidId: string;
  complianceResultId: string;
  aiRecommendation: string;
  aiConfidence: number;
  officerDecision: string;
  createdAt: string;
}
