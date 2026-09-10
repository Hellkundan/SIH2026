import { useQuery } from "@tanstack/react-query";
import { RefreshCw } from "lucide-react";

import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { getDocumentIntelligence } from "@/lib/api";
import { dateTime } from "@/lib/format";

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <dt className="text-muted-foreground">{label}</dt>
      <dd className="truncate font-medium">{value}</dd>
    </div>
  );
}

function Pending({ text }: { text: string }) {
  return (
    <div className="mt-3 rounded border border-dashed px-3 py-4 text-center">
      <StatusBadge status="PENDING" />
      <p className="mt-2 text-xs text-muted-foreground">{text}</p>
    </div>
  );
}

/**
 * OCR + registry results are fetched separately from the Document row, keyed
 * by document id. If nothing is reachable yet, we say so instead of guessing.
 */
export function DocumentIntelligencePanel({
  documentId,
  onRerun,
  rerunning,
}: {
  documentId: string;
  onRerun?: (() => void) | undefined;
  rerunning?: boolean | undefined;
}) {
  const q = useQuery({
    queryKey: ["intelligence", documentId],
    queryFn: () => getDocumentIntelligence(documentId),
  });

  const ocr = q.data?.ocr;
  const check = q.data?.verification;

  return (
    <div className="mt-4 grid gap-4 md:grid-cols-2">
      <div className="rounded-md border bg-surface p-4">
        <p className="text-xs font-semibold tracking-wide uppercase">OCR extraction</p>
        {q.isLoading ? (
          <p className="mt-3 text-sm text-muted-foreground">Reading document…</p>
        ) : ocr ? (
          <>
            <dl className="mt-3 space-y-1.5 text-sm">
              <Row label="Document number" value={ocr.documentNumber} />
              <Row label="Name on document" value={ocr.holderName} />
              {ocr.validTill ? <Row label="Valid till" value={dateTime(ocr.validTill)} /> : null}
              <Row label="Confidence" value={`${Math.round(ocr.confidence * 100)}%`} />
            </dl>
            {ocr.quality !== "GOOD" ? (
              <p className="mt-3 rounded border border-warning/40 bg-warning/12 px-2 py-1.5 text-xs text-warning-foreground">
                {ocr.quality === "BLURRY"
                  ? "Scan looks blurry — a clearer copy will verify faster."
                  : "Extraction confidence is low — please re-upload if details look wrong."}
              </p>
            ) : null}
          </>
        ) : (
          <Pending text="No extraction available yet for this document." />
        )}
      </div>

      <div className="rounded-md border bg-surface p-4">
        <div className="flex items-center justify-between gap-2">
          <p className="text-xs font-semibold tracking-wide uppercase">Registry check</p>
          {onRerun ? (
            <Button size="sm" variant="outline" onClick={onRerun} disabled={rerunning}>
              <RefreshCw className="mr-1 size-3.5" /> Re-run
            </Button>
          ) : null}
        </div>
        {q.isLoading ? (
          <p className="mt-3 text-sm text-muted-foreground">Contacting the registry…</p>
        ) : check ? (
          <>
            <dl className="mt-3 space-y-1.5 text-sm">
              <div className="flex items-center justify-between gap-3">
                <dt className="text-muted-foreground">Result</dt>
                <dd>
                  <StatusBadge status={check.status} />
                </dd>
              </div>
              <Row label="Provider" value={check.provider} />
              <Row label="Checked" value={dateTime(check.checkedAt)} />
              {check.errorState ? <Row label="Error" value={check.errorState} /> : null}
            </dl>
            {check.message ? (
              <p className="mt-3 text-xs text-muted-foreground">{check.message}</p>
            ) : null}
          </>
        ) : (
          <Pending text="Verification pending — no registry result has been returned yet." />
        )}
      </div>
    </div>
  );
}

/** Compact single-line status used inside tables and lists. */
export function DocumentCheckBadge({ documentId }: { documentId: string }) {
  const q = useQuery({
    queryKey: ["intelligence", documentId],
    queryFn: () => getDocumentIntelligence(documentId),
  });
  if (q.isLoading) return <span className="text-xs text-muted-foreground">Checking…</span>;
  return <StatusBadge status={q.data?.verification?.status ?? "PENDING"} />;
}
