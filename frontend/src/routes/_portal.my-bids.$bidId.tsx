import { useQuery } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { ArrowLeft } from "lucide-react";

import { DocumentCheckBadge } from "@/components/DocumentIntelligencePanel";
import { ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getBid, getTender, listDocumentsByBid } from "@/lib/api";
import { dateTime, inr } from "@/lib/format";
import { documentLabel } from "@/lib/types";

export const Route = createFileRoute("/_portal/my-bids/$bidId")({
  component: BidDetail,
});

function BidDetail() {
  const { bidId } = Route.useParams();

  const bid = useQuery({ queryKey: ["bid", bidId], queryFn: () => getBid(bidId) });
  const docs = useQuery({
    queryKey: ["documents", "bid", bidId],
    queryFn: () => listDocumentsByBid(bidId),
  });
  const tender = useQuery({
    queryKey: ["tender", bid.data?.tenderId],
    queryFn: () => getTender(bid.data!.tenderId),
    enabled: !!bid.data,
  });

  const attached = docs.data ?? [];

  return (
    <PortalShell title="Bid details" subtitle={bid.data?.reference}>
      <Button asChild variant="ghost" size="sm" className="mb-4 -ml-2">
        <Link to="/my-bids">
          <ArrowLeft className="mr-1 size-4" /> Back to my bids
        </Link>
      </Button>

      {bid.isLoading ? (
        <LoadingRows rows={4} />
      ) : bid.isError || !bid.data ? (
        <ErrorState message="This bid could not be loaded." />
      ) : (
        <div className="grid gap-5 lg:grid-cols-[1.6fr_1fr]">
          <div className="space-y-5">
            <Card>
              <CardContent className="p-6">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <h2 className="text-xl font-bold">{tender.data?.title ?? bid.data.tenderId}</h2>
                  <StatusBadge status={bid.data.status} />
                </div>
                <dl className="mt-5 grid gap-4 sm:grid-cols-3">
                  <div>
                    <dt className="text-xs text-muted-foreground">Quoted price</dt>
                    <dd className="font-semibold">{bid.data.quote ? inr(bid.data.quote) : "—"}</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-muted-foreground">Submitted</dt>
                    <dd className="font-semibold">
                      {bid.data.submittedAt ? dateTime(bid.data.submittedAt) : "Draft"}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-muted-foreground">Last updated</dt>
                    <dd className="font-semibold">{dateTime(bid.data.updatedAt)}</dd>
                  </div>
                </dl>
                {bid.data.notes ? (
                  <p className="mt-5 rounded-md bg-surface p-3 text-sm">{bid.data.notes}</p>
                ) : null}
                {bid.data.status === "DRAFT" ? (
                  <Button asChild className="mt-5">
                    <Link to="/bid/$tenderId" params={{ tenderId: bid.data.tenderId }}>
                      Continue this bid
                    </Link>
                  </Button>
                ) : null}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">Documents attached to this bid</CardTitle>
              </CardHeader>
              <CardContent>
                {docs.isLoading ? (
                  <LoadingRows rows={2} />
                ) : attached.length === 0 ? (
                  <p className="text-sm text-muted-foreground">
                    No documents attached to this bid.
                  </p>
                ) : (
                  <ul className="space-y-2">
                    {attached.map((d) => (
                      <li
                        key={d.id}
                        className="flex flex-wrap items-center justify-between gap-3 rounded-md border bg-background px-3 py-2.5"
                      >
                        <div className="min-w-0">
                          <p className="text-sm font-medium">{documentLabel(d.documentType)}</p>
                          <p className="truncate text-xs text-muted-foreground">
                            {d.fileName} · uploaded {dateTime(d.uploadedAt)}
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          <StatusBadge status={d.status} />
                          <DocumentCheckBadge documentId={d.id} />
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </CardContent>
            </Card>

            {bid.data.officerRemarks ? (
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Officer remarks</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm">{bid.data.officerRemarks}</p>
                </CardContent>
              </Card>
            ) : null}
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Status history</CardTitle>
            </CardHeader>
            <CardContent>
              {(bid.data.history ?? []).length === 0 ? (
                <p className="text-sm text-muted-foreground">No status changes recorded yet.</p>
              ) : (
                <ol className="space-y-4">
                  {(bid.data.history ?? []).map((h) => (
                    <li key={h.at + h.label} className="border-l-2 border-accent/40 pl-3">
                      <p className="text-sm font-medium">{h.label}</p>
                      <p className="text-xs text-muted-foreground">
                        {dateTime(h.at)} · {h.by}
                      </p>
                    </li>
                  ))}
                </ol>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </PortalShell>
  );
}
