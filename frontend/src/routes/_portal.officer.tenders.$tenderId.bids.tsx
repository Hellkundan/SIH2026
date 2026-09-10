import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { ArrowLeft } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";

import { DocumentIntelligencePanel } from "@/components/DocumentIntelligencePanel";
import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import {
  getComplianceResult,
  getRecommendation,
  getTender,
  listBidders,
  listBidsByTender,
  listDocumentsByBid,
  listRequirements,
  setBidStatus,
} from "@/lib/api";
import { dateTime, inr } from "@/lib/format";
import { documentLabel } from "@/lib/types";

export const Route = createFileRoute("/_portal/officer/tenders/$tenderId/bids")({
  component: TenderBids,
});

function ComplianceRecommendationSummary({ bidId }: { bidId: string }) {
  const comp = useQuery({
    queryKey: ["compliance", bidId],
    queryFn: () => getComplianceResult(bidId),
  });
  const rec = useQuery({
    queryKey: ["recommendation", bidId],
    queryFn: () => getRecommendation(bidId),
  });

  if (comp.isLoading || rec.isLoading) return null;
  if (!comp.data && !rec.data) return null;

  return (
    <div className="mt-4 rounded-md border bg-muted/30 p-4 space-y-2">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase text-muted-foreground">
            AI Intelligence Recommendation:
          </span>
          <span className="rounded bg-primary/10 px-2 py-0.5 text-xs font-bold text-primary">
            {rec.data?.aiRecommendation ?? "EVALUATED"}
          </span>
        </div>
        {comp.data?.severity ? (
          <span
            className={`text-xs px-2 py-0.5 rounded font-medium ${
              comp.data.severity === "LOW"
                ? "bg-emerald-100 text-emerald-800"
                : comp.data.severity === "MEDIUM"
                  ? "bg-yellow-100 text-yellow-800"
                  : "bg-red-100 text-red-800"
            }`}
          >
            Risk: {comp.data.severity}
          </span>
        ) : null}
      </div>
      {comp.data?.explanation ? (
        <p className="text-xs text-muted-foreground">{comp.data.explanation}</p>
      ) : null}
    </div>
  );
}

function TenderBids() {
  const { tenderId } = Route.useParams();
  const qc = useQueryClient();

  const tender = useQuery({ queryKey: ["tender", tenderId], queryFn: () => getTender(tenderId) });
  const bids = useQuery({
    queryKey: ["bids", "tender", tenderId],
    queryFn: () => listBidsByTender(tenderId),
  });
  const bidders = useQuery({ queryKey: ["bidders"], queryFn: listBidders });
  const reqs = useQuery({
    queryKey: ["requirements", tenderId],
    queryFn: () => listRequirements(tenderId),
  });

  const [selected, setSelected] = useState<string | null>(null);
  const [remarks, setRemarks] = useState("");

  const act = useMutation({
    mutationFn: ({ id, action }: { id: string; action: "review" | "qualify" | "disqualify" }) =>
      setBidStatus(id, action, remarks || undefined),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bids"] });
      setRemarks("");
      toast.success("Bid status updated");
    },
    onError: () => toast.error("The bid status could not be updated."),
  });

  const list = (bids.data ?? []).filter((b) => b.status !== "DRAFT");
  const companyOf = (id: string) => bidders.data?.find((b) => b.id === id)?.companyName ?? id;

  return (
    <PortalShell title="Bid review" subtitle={tender.data?.title ?? tenderId}>
      <Button asChild variant="ghost" size="sm" className="mb-4 -ml-2">
        <Link to="/officer/tenders">
          <ArrowLeft className="mr-1 size-4" /> Back to tenders
        </Link>
      </Button>

      {(reqs.data ?? []).length > 0 ? (
        <Card className="mb-5">
          <CardContent className="p-5">
            <p className="text-sm font-semibold">Published requirements</p>
            <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
              {(reqs.data ?? []).map((r) => (
                <li key={r.id}>
                  {r.requirement} {r.mandatory ? "(mandatory)" : "(preferred)"}
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      ) : null}

      {bids.isLoading ? (
        <LoadingRows rows={3} />
      ) : bids.isError ? (
        <ErrorState />
      ) : list.length === 0 ? (
        <EmptyState
          title="No submitted bids yet"
          description="Bids appear here as soon as vendors submit them."
        />
      ) : (
        <div className="space-y-4">
          {list.map((bid) => (
            <Card key={bid.id}>
              <CardContent className="p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h3 className="font-semibold">{companyOf(bid.bidderId)}</h3>
                    <p className="text-xs text-muted-foreground">
                      {bid.reference} · quoted {bid.quote ? inr(bid.quote) : "—"} ·{" "}
                      {bid.submittedAt ? dateTime(bid.submittedAt) : "not submitted"}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <StatusBadge status={bid.status} />
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setSelected(selected === bid.id ? null : bid.id)}
                    >
                      {selected === bid.id ? "Hide documents" : "Review documents"}
                    </Button>
                  </div>
                </div>

                <ComplianceRecommendationSummary bidId={bid.id} />

                {bid.notes ? (
                  <p className="mt-3 rounded-md bg-surface p-3 text-sm">{bid.notes}</p>
                ) : null}

                {selected === bid.id ? <BidDocuments bidId={bid.id} /> : null}

                <div className="mt-5 space-y-3 border-t pt-4">
                  <Textarea
                    rows={2}
                    placeholder="Remarks for the audit trail (optional)"
                    value={selected === bid.id ? remarks : ""}
                    onFocus={() => setSelected(bid.id)}
                    onChange={(e) => setRemarks(e.target.value)}
                  />
                  <div className="flex flex-wrap gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => act.mutate({ id: bid.id, action: "review" })}
                    >
                      Mark under review
                    </Button>
                    <Button size="sm" onClick={() => act.mutate({ id: bid.id, action: "qualify" })}>
                      Qualify
                    </Button>
                    <Button
                      size="sm"
                      variant="destructive"
                      onClick={() => act.mutate({ id: bid.id, action: "disqualify" })}
                    >
                      Disqualify
                    </Button>
                  </div>
                  {bid.officerRemarks ? (
                    <p className="text-xs text-muted-foreground">
                      Last remark: {bid.officerRemarks}
                    </p>
                  ) : null}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </PortalShell>
  );
}

function BidDocuments({ bidId }: { bidId: string }) {
  const docs = useQuery({
    queryKey: ["documents", "bid", bidId],
    queryFn: () => listDocumentsByBid(bidId),
  });

  if (docs.isLoading)
    return (
      <div className="mt-4">
        <LoadingRows rows={2} />
      </div>
    );
  if (docs.isError)
    return (
      <div className="mt-4">
        <ErrorState />
      </div>
    );

  const list = docs.data ?? [];
  if (list.length === 0) {
    return (
      <p className="mt-4 rounded-md border border-dashed px-3 py-4 text-sm text-muted-foreground">
        This bid has no documents attached.
      </p>
    );
  }

  return (
    <div className="mt-4 space-y-4">
      {list.map((d) => (
        <div key={d.id} className="rounded-md border p-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="text-sm font-medium">{documentLabel(d.documentType)}</p>
              <p className="text-xs text-muted-foreground">
                {d.fileName} · uploaded {dateTime(d.uploadedAt)}
              </p>
            </div>
            <StatusBadge status={d.status} />
          </div>
          <DocumentIntelligencePanel documentId={d.id} />
        </div>
      ))}
    </div>
  );
}
