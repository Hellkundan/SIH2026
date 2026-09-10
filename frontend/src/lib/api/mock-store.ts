import type {
  ActivityItem,
  Bidder,
  DocumentIntelligence,
  Tender,
  TenderBid,
  TenderDocument,
  TenderRequirement,
  VerificationResult,
} from "../types";

const day = 86_400_000;
const now = Date.now();
const iso = (offsetDays: number) => new Date(now + offsetDays * day).toISOString();

export const tenders: Tender[] = [
  {
    id: "TND-1001",
    title: "Supply of Solar Street Lighting Units — Phase III",
    description:
      "Design, supply, installation and five-year maintenance of 4,200 LED solar street lighting units across twelve municipal wards. Bidders must hold valid MSME registration and demonstrate prior deployment of at least 1,000 units.",
    status: "OPEN",
    createdAt: iso(-24),
  },
  {
    id: "TND-1002",
    title: "Annual Rate Contract for Laboratory Consumables",
    description:
      "Rate contract for supply of laboratory reagents and consumables to eighteen government medical colleges for the financial year, with quarterly delivery schedules.",
    status: "OPEN",
    createdAt: iso(-13),
  },
  {
    id: "TND-1003",
    title: "Managed Network Services for District Data Centres",
    description:
      "Provision of managed SD-WAN connectivity, monitoring and 24x7 NOC support for nine district data centres, including OEM-backed hardware.",
    status: "OPEN",
    createdAt: iso(-6),
  },
  {
    id: "TND-1004",
    title: "Uniform and Protective Gear for Sanitation Workers",
    description:
      "Manufacture and supply of 26,000 sets of uniforms, gloves and safety footwear conforming to prescribed BIS standards.",
    status: "CLOSED",
    createdAt: iso(-72),
  },
  {
    id: "TND-1005",
    title: "Rooftop Rainwater Harvesting Retrofit for Schools",
    description:
      "Survey, design and retrofit of rainwater harvesting systems in 310 government schools, with water quality testing for two monsoon cycles.",
    status: "DRAFT",
    createdAt: iso(-2),
  },
];

export const bidders: Bidder[] = [
  {
    id: "BDR-501",
    companyName: "Aarav Infratech Solutions Pvt Ltd",
    email: "contact@aaravinfratech.in",
    phone: "+91 98200 41122",
    createdAt: iso(-210),
  },
  {
    id: "BDR-502",
    companyName: "Meridian Labworks LLP",
    email: "contact@meridianlabworks.in",
    phone: "+91 91450 77310",
    createdAt: iso(-140),
  },
  {
    id: "BDR-503",
    companyName: "Northline Systems & Networks",
    email: "tenders@northlinesys.co.in",
    phone: "+91 99870 20045",
    createdAt: iso(-95),
  },
];

export const requirements: TenderRequirement[] = [
  {
    id: "REQ-1",
    tenderId: "TND-1001",
    requirement: "Valid PAN of the bidding entity",
    mandatory: true,
  },
  {
    id: "REQ-2",
    tenderId: "TND-1001",
    requirement: "Active GST registration in the state of supply",
    mandatory: true,
  },
  {
    id: "REQ-3",
    tenderId: "TND-1001",
    requirement: "Udyam/MSME registration in the Small or Medium category",
    mandatory: true,
  },
  {
    id: "REQ-4",
    tenderId: "TND-1001",
    requirement: "EPFO establishment code with at least 50 contributing members",
    mandatory: true,
  },
  {
    id: "REQ-5",
    tenderId: "TND-1001",
    requirement: "NSIC single point registration certificate",
    mandatory: false,
  },
  {
    id: "REQ-6",
    tenderId: "TND-1002",
    requirement: "Valid PAN of the bidding entity",
    mandatory: true,
  },
  { id: "REQ-7", tenderId: "TND-1002", requirement: "Active GST registration", mandatory: true },
  {
    id: "REQ-8",
    tenderId: "TND-1002",
    requirement: "Active ESIC registration code",
    mandatory: true,
  },
  {
    id: "REQ-9",
    tenderId: "TND-1003",
    requirement: "Valid PAN of the bidding entity",
    mandatory: true,
  },
  { id: "REQ-10", tenderId: "TND-1003", requirement: "Active GST registration", mandatory: true },
  {
    id: "REQ-11",
    tenderId: "TND-1003",
    requirement: "OEM authorization letter from the network hardware manufacturer",
    mandatory: true,
  },
  {
    id: "REQ-12",
    tenderId: "TND-1003",
    requirement: "Make in India local content declaration of at least 50%",
    mandatory: false,
  },
];

export const bids: TenderBid[] = [
  {
    id: "BID-3001",
    reference: "DIXY/2026/BID/3001",
    tenderId: "TND-1001",
    bidderId: "BDR-501",
    status: "UNDER_REVIEW",
    quote: 46250000,
    notes: "Includes five-year comprehensive maintenance and remote monitoring dashboard.",
    submittedAt: iso(-5),
    updatedAt: iso(-2),
    history: [
      { at: iso(-7), label: "Bid created as draft", by: "Aarav Infratech" },
      { at: iso(-5), label: "Bid submitted", by: "Aarav Infratech" },
      { at: iso(-2), label: "Marked under review", by: "Officer R. Menon" },
    ],
  },
  {
    id: "BID-3002",
    reference: "DIXY/2026/BID/3002",
    tenderId: "TND-1002",
    bidderId: "BDR-501",
    status: "SUBMITTED",
    quote: 11980000,
    submittedAt: iso(-1),
    updatedAt: iso(-1),
    history: [
      { at: iso(-2), label: "Bid created as draft", by: "Aarav Infratech" },
      { at: iso(-1), label: "Bid submitted", by: "Aarav Infratech" },
    ],
  },
  {
    id: "BID-3003",
    reference: "DIXY/2026/BID/3003",
    tenderId: "TND-1003",
    bidderId: "BDR-501",
    status: "DRAFT",
    quote: 0,
    updatedAt: iso(-1),
    history: [{ at: iso(-1), label: "Bid created as draft", by: "Aarav Infratech" }],
  },
  {
    id: "BID-3004",
    reference: "DIXY/2026/BID/3004",
    tenderId: "TND-1002",
    bidderId: "BDR-502",
    status: "QUALIFIED",
    quote: 12440000,
    submittedAt: iso(-4),
    updatedAt: iso(-2),
    officerRemarks: "All mandatory checks passed. Recommended for technical evaluation.",
    history: [
      { at: iso(-4), label: "Bid submitted", by: "Meridian Labworks" },
      { at: iso(-3), label: "Marked under review", by: "Officer R. Menon" },
      { at: iso(-2), label: "Qualified", by: "Officer R. Menon" },
    ],
  },
  {
    id: "BID-3005",
    reference: "DIXY/2026/BID/3005",
    tenderId: "TND-1003",
    bidderId: "BDR-503",
    status: "DISQUALIFIED",
    quote: 87900000,
    submittedAt: iso(-6),
    updatedAt: iso(-3),
    officerRemarks:
      "Bidder appears on the central debarment list and the submitted GSTIN could not be traced.",
    history: [
      { at: iso(-6), label: "Bid submitted", by: "Northline Systems" },
      { at: iso(-5), label: "Marked under review", by: "Officer R. Menon" },
      { at: iso(-3), label: "Disqualified", by: "Officer R. Menon" },
    ],
  },
  {
    id: "BID-3006",
    reference: "DIXY/2026/BID/3006",
    tenderId: "TND-1001",
    bidderId: "BDR-502",
    status: "SUBMITTED",
    quote: 49900000,
    submittedAt: iso(-2),
    updatedAt: iso(-2),
    history: [{ at: iso(-2), label: "Bid submitted", by: "Meridian Labworks" }],
  },
];

/** Documents always hang off a TenderBid — there is no bidder-wide library. */
export const documents: TenderDocument[] = [
  {
    id: "DOC-9001",
    tenderBidId: "BID-3001",
    documentType: "PAN",
    fileName: "pan-aarav-infratech.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-7),
  },
  {
    id: "DOC-9002",
    tenderBidId: "BID-3001",
    documentType: "GST",
    fileName: "gst-certificate.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-7),
  },
  {
    id: "DOC-9003",
    tenderBidId: "BID-3001",
    documentType: "UDYAM",
    fileName: "udyam-registration.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-7),
  },
  {
    id: "DOC-9004",
    tenderBidId: "BID-3001",
    documentType: "EPFO",
    fileName: "epfo-establishment.jpg",
    status: "PROCESSED",
    uploadedAt: iso(-6),
  },
  {
    id: "DOC-9005",
    tenderBidId: "BID-3001",
    documentType: "NSIC",
    fileName: "nsic-certificate.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-6),
  },
  {
    id: "DOC-9006",
    tenderBidId: "BID-3002",
    documentType: "PAN",
    fileName: "pan-aarav-infratech.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-2),
  },
  {
    id: "DOC-9007",
    tenderBidId: "BID-3002",
    documentType: "ESIC",
    fileName: "esic-code.pdf",
    status: "FAILED",
    uploadedAt: iso(-2),
  },
  {
    id: "DOC-9101",
    tenderBidId: "BID-3004",
    documentType: "PAN",
    fileName: "meridian-pan.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-4),
  },
  {
    id: "DOC-9102",
    tenderBidId: "BID-3004",
    documentType: "GST",
    fileName: "meridian-gst.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-4),
  },
  {
    id: "DOC-9103",
    tenderBidId: "BID-3005",
    documentType: "GST",
    fileName: "northline-gst.jpg",
    status: "PROCESSED",
    uploadedAt: iso(-6),
  },
  {
    id: "DOC-9104",
    tenderBidId: "BID-3006",
    documentType: "PAN",
    fileName: "meridian-pan.pdf",
    status: "VERIFIED",
    uploadedAt: iso(-2),
  },
];

const verification = (
  type: string,
  status: VerificationResult["status"],
  provider: string,
  extras: Partial<VerificationResult> = {},
): VerificationResult => ({
  type,
  status,
  provider,
  errorState: null,
  checkedAt: iso(-3),
  ...extras,
});

/**
 * Stands in for the Document Intelligence + Verification Hub services, which
 * are separate from the Document rows above.
 */
export const intelligence: Record<string, DocumentIntelligence> = {
  "DOC-9001": {
    documentId: "DOC-9001",
    ocr: {
      documentNumber: "AACCA4821K",
      holderName: "Aarav Infratech Solutions Pvt Ltd",
      confidence: 0.97,
      quality: "GOOD",
    },
    verification: verification("PAN", "VERIFIED", "Income Tax Department"),
  },
  "DOC-9002": {
    documentId: "DOC-9002",
    ocr: {
      documentNumber: "27AACCA4821K1ZP",
      holderName: "Aarav Infratech Solutions Pvt Ltd",
      issuedOn: iso(-900),
      confidence: 0.94,
      quality: "GOOD",
    },
    verification: verification("GST", "VERIFIED", "GSTN Public API"),
  },
  "DOC-9003": {
    documentId: "DOC-9003",
    ocr: {
      documentNumber: "UDYAM-MH-26-0044817",
      holderName: "Aarav Infratech Solutions Pvt Ltd",
      issuedOn: iso(-700),
      confidence: 0.91,
      quality: "GOOD",
    },
    verification: verification("UDYAM", "VERIFIED", "Udyam Registration Portal"),
  },
  "DOC-9004": {
    documentId: "DOC-9004",
    ocr: {
      documentNumber: "MHPUN0043912000",
      holderName: "Aarav Infratech Solutions",
      confidence: 0.63,
      quality: "LOW_CONFIDENCE",
    },
    verification: verification("EPFO", "MANUAL_REVIEW", "EPFO Establishment Search", {
      message: "Name on document differs from registered company name.",
    }),
  },
  "DOC-9005": {
    documentId: "DOC-9005",
    ocr: {
      documentNumber: "NSIC/SPRC/PUN/2291",
      holderName: "Aarav Infratech Solutions Pvt Ltd",
      validTill: iso(240),
      confidence: 0.93,
      quality: "GOOD",
    },
    verification: verification("NSIC", "VERIFIED", "NSIC Single Point Registration"),
  },
  "DOC-9006": {
    documentId: "DOC-9006",
    ocr: {
      documentNumber: "AACCA4821K",
      holderName: "Aarav Infratech Solutions Pvt Ltd",
      confidence: 0.96,
      quality: "GOOD",
    },
    verification: verification("PAN", "VERIFIED", "Income Tax Department"),
  },
  "DOC-9007": {
    documentId: "DOC-9007",
    ocr: {
      documentNumber: "31000456780001099",
      holderName: "Aarav Infratech Solutions Pvt Ltd",
      confidence: 0.88,
      quality: "GOOD",
    },
    verification: verification("ESIC", "FAILED", "ESIC Portal", {
      errorState: "PROVIDER_UNAVAILABLE",
      message: "Registry did not respond after three attempts.",
    }),
  },
  "DOC-9101": {
    documentId: "DOC-9101",
    ocr: {
      documentNumber: "AAGFM9921L",
      holderName: "Meridian Labworks LLP",
      confidence: 0.96,
      quality: "GOOD",
    },
    verification: verification("PAN", "VERIFIED", "Income Tax Department"),
  },
  "DOC-9102": {
    documentId: "DOC-9102",
    ocr: {
      documentNumber: "36AAGFM9921L1Z4",
      holderName: "Meridian Labworks LLP",
      confidence: 0.95,
      quality: "GOOD",
    },
    verification: verification("GST", "VERIFIED", "GSTN Public API"),
  },
  "DOC-9103": {
    documentId: "DOC-9103",
    ocr: {
      documentNumber: "09AAEFN1187Q1ZR",
      holderName: "Northline Systems & Networks",
      confidence: 0.58,
      quality: "BLURRY",
    },
    verification: verification("GST", "NOT_FOUND", "GSTN Public API", {
      message: "No active registration found for the extracted GSTIN.",
    }),
  },
  "DOC-9104": {
    documentId: "DOC-9104",
    ocr: {
      documentNumber: "AAGFM9921L",
      holderName: "Meridian Labworks LLP",
      confidence: 0.96,
      quality: "GOOD",
    },
    verification: verification("PAN", "VERIFIED", "Income Tax Department"),
  },
};

/** Debarment screening is a Verification Hub call, not a Bidder field. */
const debarred: Record<string, string> = {
  "BDR-503":
    "Debarred for 24 months by the Central Procurement Cell for submission of a forged performance certificate.",
};

export const blacklistCheck = (bidderId: string): VerificationResult =>
  verification(
    "BLACKLIST",
    debarred[bidderId] ? "FAILED" : "VERIFIED",
    "Central Debarment Registry",
    debarred[bidderId] ? { message: debarred[bidderId] } : {},
  );

export const activity: ActivityItem[] = [
  { at: iso(-1), label: "ESIC document uploaded to bid DIXY/2026/BID/3002" },
  { at: iso(-1), label: "Bid DIXY/2026/BID/3002 submitted for Laboratory Consumables" },
  { at: iso(-2), label: "Bid DIXY/2026/BID/3001 marked under review by procurement officer" },
  { at: iso(-3), label: "ESIC verification failed — registry unavailable" },
  { at: iso(-3), label: "EPFO verification flagged for manual review" },
  { at: iso(-6), label: "NSIC registration verified" },
];

export const nextId = (prefix: string) => `${prefix}-${Math.floor(Math.random() * 9000 + 1000)}`;

export const nowIso = () => new Date().toISOString();
