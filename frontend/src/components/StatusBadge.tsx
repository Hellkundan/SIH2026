import { cn } from "@/lib/utils";
import { titleCase } from "@/lib/format";
import type { BidStatus, DocumentStatus, TenderStatus, VerificationStatus } from "@/lib/types";

type Tone = "neutral" | "success" | "warning" | "danger" | "info";

const toneClass: Record<Tone, string> = {
  neutral: "bg-muted text-muted-foreground border-border",
  success: "bg-success/12 text-success border-success/30",
  warning: "bg-warning/18 text-warning-foreground border-warning/40",
  danger: "bg-destructive/10 text-destructive border-destructive/30",
  info: "bg-info/12 text-info border-info/30",
};

const tones: Record<string, Tone> = {
  // tender
  DRAFT: "neutral",
  OPEN: "success",
  CLOSED: "danger",
  // bid
  SUBMITTED: "info",
  UNDER_REVIEW: "warning",
  QUALIFIED: "success",
  DISQUALIFIED: "danger",
  // verification
  VERIFIED: "success",
  NOT_FOUND: "warning",
  FAILED: "danger",
  PENDING: "neutral",
  MANUAL_REVIEW: "warning",
  // document
  UPLOADED: "neutral",
  PROCESSING: "info",
  PROCESSED: "warning",
};

export function StatusBadge({
  status,
  className,
}: {
  status: TenderStatus | BidStatus | VerificationStatus | DocumentStatus | string;
  className?: string;
}) {
  const tone = tones[status] ?? "neutral";
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-semibold whitespace-nowrap",
        toneClass[tone],
        className,
      )}
    >
      <span
        className={cn(
          "size-1.5 rounded-full",
          tone === "success" && "bg-success",
          tone === "warning" && "bg-warning",
          tone === "danger" && "bg-destructive",
          tone === "info" && "bg-info",
          tone === "neutral" && "bg-muted-foreground",
        )}
      />
      {titleCase(status)}
    </span>
  );
}
