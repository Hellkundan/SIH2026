import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, createFileRoute, useNavigate } from "@tanstack/react-router";
import { CheckCircle2, CircleDot, FileUp, RefreshCw, Trash2, UploadCloud } from "lucide-react";
import { useRef, useState } from "react";
import { toast } from "sonner";

import { DocumentIntelligencePanel } from "@/components/DocumentIntelligencePanel";
import { ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import {
  deleteDocument,
  ensureDraftBid,
  getTender,
  listBidsByBidder,
  listDocumentsByBid,
  listRequirements,
  runVerification,
  saveBid,
  uploadDocument,
} from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { dateTime, inr } from "@/lib/format";
import { DOCUMENT_LABELS, DOCUMENT_TYPES, documentLabel } from "@/lib/types";
import type { DocumentType } from "@/lib/types";

export const Route = createFileRoute("/_portal/bid/$tenderId")({
  component: BidWizard,
});

const STEPS = ["Requirements", "Documents", "Bid details", "Review & submit"];

function BidWizard() {
  const { tenderId } = Route.useParams();
  const { bidder } = useAuth();
  const bidderId = bidder?.id ?? "";
  const navigate = useNavigate();
  const qc = useQueryClient();

  const tender = useQuery({ queryKey: ["tender", tenderId], queryFn: () => getTender(tenderId) });
  const reqs = useQuery({
    queryKey: ["requirements", tenderId],
    queryFn: () => listRequirements(tenderId),
  });

  /** Documents need a bid, so the draft bid is created before anything else. */
  const draft = useQuery({
    queryKey: ["draft-bid", tenderId, bidderId],
    queryFn: () => ensureDraftBid(tenderId, bidderId),
    enabled: !!bidderId,
  });
  const bidId = draft.data?.id ?? "";

  const docs = useQuery({
    queryKey: ["documents", "bid", bidId],
    queryFn: () => listDocumentsByBid(bidId),
    enabled: !!bidId,
  });

  const [step, setStep] = useState(0);
  const [quote, setQuote] = useState("");
  const [notes, setNotes] = useState("");
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);

  const refreshDocs = () => qc.invalidateQueries({ queryKey: ["documents", "bid", bidId] });

  const upload = useMutation({
    mutationFn: (input: { documentType: DocumentType; fileName: string; file?: File }) =>
      uploadDocument({ tenderBidId: bidId, ...input }),
    onSuccess: async (doc) => {
      await refreshDocs();
      toast.success(`${documentLabel(doc.documentType)} uploaded`);
      const result = await runVerification(doc.id);
      qc.invalidateQueries({ queryKey: ["intelligence", doc.id] });
      await refreshDocs();
      if (!result) toast.info("Uploaded — verification is still pending.");
    },
    onError: () => toast.error("Upload failed. Please try again."),
  });

  const remove = useMutation({
    mutationFn: (id: string) => deleteDocument(id),
    onSuccess: () => {
      refreshDocs();
      toast.success("Document removed from this bid");
    },
    onError: () => toast.error("The document could not be removed."),
  });

  const reverify = useMutation({
    mutationFn: (id: string) => runVerification(id),
    onSuccess: (result, id) => {
      qc.invalidateQueries({ queryKey: ["intelligence", id] });
      refreshDocs();
      toast[result ? "success" : "info"](
        result ? "Verification re-run" : "Verification service did not respond yet",
      );
    },
    onError: () => toast.error("The registry could not be reached. Try again shortly."),
  });

  async function persist(submit: boolean) {
    if (!bidId) return;
    setBusy(true);
    try {
      await saveBid({
        id: bidId,
        tenderId,
        bidderId,
        quote: Number(quote || 0),
        notes: notes || undefined,
        submit,
      });
      qc.invalidateQueries({ queryKey: ["bids", bidderId] });
      if (submit) {
        toast.success("Bid submitted");
        setDone(true);
      } else {
        toast.success("Draft saved");
      }
    } catch {
      toast.error("We couldn't save your bid. Please try again.");
    } finally {
      setBusy(false);
    }
  }

  if (draft.isLoading || tender.isLoading) {
    return (
      <PortalShell title="Submit a bid" subtitle="Preparing your draft bid…">
        <LoadingRows rows={4} />
      </PortalShell>
    );
  }

  if (draft.isError || !draft.data) {
    return (
      <PortalShell title="Submit a bid">
        <ErrorState message="We couldn't open a draft bid for this tender." />
      </PortalShell>
    );
  }

  if (done) {
    return (
      <PortalShell title="Bid submitted">
        <Card className="mx-auto max-w-lg">
          <CardContent className="p-8 text-center">
            <CheckCircle2 className="mx-auto size-12 text-success" />
            <h2 className="mt-4 text-xl font-bold">Your bid is in</h2>
            <p className="mt-2 text-sm text-muted-foreground">
              Reference{" "}
              <span className="font-semibold text-foreground">{draft.data.reference}</span>. The
              procurement officer typically completes the first review within five working days.
            </p>
            <div className="mt-6 flex justify-center gap-3">
              <Button asChild>
                <Link to="/my-bids">Track my bids</Link>
              </Button>
              <Button asChild variant="outline">
                <Link to="/tenders">Browse more tenders</Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </PortalShell>
    );
  }

  const docList = docs.data ?? [];

  return (
    <PortalShell title="Submit a bid" subtitle={tender.data?.title}>
      <p className="mb-4 text-xs text-muted-foreground">
        Draft bid <span className="font-medium text-foreground">{draft.data.reference}</span> —
        documents you upload are attached to this bid.
      </p>

      <ol className="mb-6 flex flex-wrap gap-2">
        {STEPS.map((s, i) => (
          <li
            key={s}
            className={`rounded-full border px-3 py-1 text-xs font-semibold ${
              i === step
                ? "border-primary bg-primary text-primary-foreground"
                : i < step
                  ? "border-success/40 bg-success/10 text-success"
                  : "border-border text-muted-foreground"
            }`}
          >
            {i + 1}. {s}
          </li>
        ))}
      </ol>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">{STEPS[step]}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {step === 0 ? (
            reqs.isLoading ? (
              <LoadingRows rows={3} />
            ) : (reqs.data ?? []).length === 0 ? (
              <p className="text-sm text-muted-foreground">
                No requirements were published for this tender.
              </p>
            ) : (
              <ul className="space-y-2">
                {(reqs.data ?? []).map((r) => (
                  <li
                    key={r.id}
                    className="flex items-start gap-3 rounded-md border bg-background px-3 py-2.5"
                  >
                    <CircleDot
                      className={`mt-0.5 size-4 shrink-0 ${
                        r.mandatory ? "text-primary" : "text-muted-foreground"
                      }`}
                    />
                    <span className="text-sm">
                      {r.requirement}{" "}
                      <span className="text-xs text-muted-foreground">
                        {r.mandatory ? "(mandatory)" : "(preferred)"}
                      </span>
                    </span>
                  </li>
                ))}
              </ul>
            )
          ) : null}

          {step === 1 ? (
            <DocumentsStep
              bidId={bidId}
              bidderId={bidderId}
              docs={docList}
              loading={docs.isLoading}
              uploading={upload.isPending}
              onUpload={(documentType, fileName, file) => upload.mutate({ documentType, fileName, file })}
              onRemove={(id) => remove.mutate(id)}
              onRerun={(id) => reverify.mutate(id)}
              rerunning={reverify.isPending}
            />
          ) : null}

          {step === 2 ? (
            <div className="grid gap-4 sm:max-w-md">
              <div className="space-y-2">
                <Label htmlFor="quote">Quoted price (INR)</Label>
                <Input
                  id="quote"
                  type="number"
                  min={0}
                  value={quote}
                  onChange={(e) => setQuote(e.target.value)}
                  placeholder="e.g. 46250000"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="notes">Notes for the evaluation committee</Label>
                <Textarea
                  id="notes"
                  rows={5}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Delivery timeline, warranty terms, deviations…"
                />
              </div>
            </div>
          ) : null}

          {step === 3 ? (
            <dl className="grid gap-3 text-sm sm:max-w-lg">
              <div className="flex justify-between border-b pb-2">
                <dt className="text-muted-foreground">Tender</dt>
                <dd className="text-right font-medium">{tender.data?.title}</dd>
              </div>
              <div className="flex justify-between border-b pb-2">
                <dt className="text-muted-foreground">Requirements listed</dt>
                <dd className="font-medium">{(reqs.data ?? []).length}</dd>
              </div>
              <div className="flex justify-between border-b pb-2">
                <dt className="text-muted-foreground">Documents attached</dt>
                <dd className="font-medium">{docList.length}</dd>
              </div>
              <div className="flex justify-between border-b pb-2">
                <dt className="text-muted-foreground">Quoted price</dt>
                <dd className="font-medium">{quote ? inr(Number(quote)) : "Not entered"}</dd>
              </div>
              {notes ? (
                <div>
                  <dt className="text-muted-foreground">Notes</dt>
                  <dd className="mt-1">{notes}</dd>
                </div>
              ) : null}
            </dl>
          ) : null}
        </CardContent>
      </Card>

      <div className="mt-5 flex flex-wrap items-center justify-between gap-3">
        <Button
          variant="ghost"
          onClick={() => (step === 0 ? navigate({ to: "/tenders" }) : setStep((s) => s - 1))}
        >
          {step === 0 ? "Cancel" : "Back"}
        </Button>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => persist(false)} disabled={busy}>
            Save as draft
          </Button>
          {step < 3 ? (
            <Button onClick={() => setStep((s) => s + 1)}>Continue</Button>
          ) : (
            <Button onClick={() => persist(true)} disabled={busy || !quote}>
              {busy ? "Submitting…" : "Submit bid"}
            </Button>
          )}
        </div>
      </div>
    </PortalShell>
  );
}

function DocumentsStep({
  bidId,
  bidderId,
  docs,
  loading,
  uploading,
  onUpload,
  onRemove,
  onRerun,
  rerunning,
}: {
  bidId: string;
  bidderId: string;
  docs: {
    id: string;
    documentType: DocumentType;
    fileName: string;
    status: string;
    uploadedAt: string;
  }[];
  loading: boolean;
  uploading: boolean;
  onUpload: (type: DocumentType, fileName: string, file?: File) => void;
  onRemove: (id: string) => void;
  onRerun: (id: string) => void;
  rerunning: boolean;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [type, setType] = useState<DocumentType>("PAN");
  const [dragging, setDragging] = useState(false);

  // Documents from the bidder's other bids, offered for a manual re-attach.
  const otherBids = useQuery({
    queryKey: ["bids", bidderId],
    queryFn: () => listBidsByBidder(bidderId),
    enabled: !!bidderId,
  });
  const previous = (otherBids.data ?? []).filter((b) => b.id !== bidId);
  const previousDocs = useQueries({
    queries: previous.map((b) => ({
      queryKey: ["documents", "bid", b.id],
      queryFn: () => listDocumentsByBid(b.id),
    })),
  }).flatMap((q) => q.data ?? []);

  const reusable = previousDocs.filter(
    (p) => !docs.some((d) => d.documentType === p.documentType && d.fileName === p.fileName),
  );

  const handleFiles = (files: FileList | null) => {
    const file = files?.[0];
    if (!file) return;
    onUpload(type, file.name, file);
  };

  return (
    <div className="space-y-5">
      <div className="grid gap-3 sm:grid-cols-[220px_1fr]">
        <Select value={type} onValueChange={(v) => setType(v as DocumentType)}>
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {DOCUMENT_TYPES.map((t) => (
              <SelectItem key={t} value={t}>
                {DOCUMENT_LABELS[t]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <div
          onDragOver={(e) => {
            e.preventDefault();
            setDragging(true);
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={(e) => {
            e.preventDefault();
            setDragging(false);
            handleFiles(e.dataTransfer.files);
          }}
          onClick={() => inputRef.current?.click()}
          className={`flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed px-6 py-8 text-center transition-colors ${
            dragging ? "border-accent bg-accent/5" : "border-border bg-background"
          }`}
        >
          <UploadCloud className="size-6 text-accent" />
          <p className="mt-2 text-sm font-medium">
            {uploading ? "Uploading and reading document…" : "Drop a file here or click to browse"}
          </p>
          <p className="mt-1 text-xs text-muted-foreground">PDF, JPG or PNG up to 10 MB</p>
          <input
            ref={inputRef}
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            className="hidden"
            onChange={(e) => handleFiles(e.target.files)}
          />
        </div>
      </div>

      {reusable.length > 0 ? (
        <div className="rounded-md border bg-surface p-4">
          <p className="text-sm font-semibold">Reuse a document from an earlier bid</p>
          <p className="mt-0.5 text-xs text-muted-foreground">
            Documents belong to a single bid, so this re-uploads a fresh copy against this bid.
          </p>
          <ul className="mt-3 space-y-2">
            {reusable.map((p) => (
              <li
                key={p.id}
                className="flex flex-wrap items-center justify-between gap-2 rounded border bg-background px-3 py-2 text-sm"
              >
                <span className="min-w-0 truncate">
                  {documentLabel(p.documentType)}
                  <span className="ml-2 text-xs text-muted-foreground">{p.fileName}</span>
                </span>
                <Button
                  size="sm"
                  variant="outline"
                  disabled={uploading}
                  onClick={() => onUpload(p.documentType, p.fileName)}
                >
                  Re-attach
                </Button>
              </li>
            ))}
          </ul>
        </div>
      ) : null}

      {loading ? (
        <LoadingRows rows={2} />
      ) : docs.length === 0 ? (
        <p className="text-sm text-muted-foreground">No documents attached to this bid yet.</p>
      ) : (
        <div className="space-y-4">
          {docs.map((d) => (
            <Card key={d.id}>
              <CardContent className="p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h3 className="font-semibold">{documentLabel(d.documentType)}</h3>
                    <p className="text-xs text-muted-foreground">
                      <FileUp className="mr-1 inline size-3" />
                      {d.fileName} · uploaded {dateTime(d.uploadedAt)}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <StatusBadge status={d.status} />
                    <Button
                      size="sm"
                      variant="ghost"
                      aria-label="Remove document"
                      onClick={() => onRemove(d.id)}
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  </div>
                </div>
                <DocumentIntelligencePanel
                  documentId={d.id}
                  onRerun={() => onRerun(d.id)}
                  rerunning={rerunning}
                />
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {rerunning ? (
        <p className="flex items-center gap-2 text-xs text-muted-foreground">
          <RefreshCw className="size-3 animate-spin" /> Re-checking with the registry…
        </p>
      ) : null}
    </div>
  );
}
