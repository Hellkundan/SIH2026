import { useQueries, useQuery } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { ArrowLeft, ShieldAlert } from "lucide-react";

import { DocumentIntelligencePanel } from "@/components/DocumentIntelligencePanel";
import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  getBidder,
  getBlacklistCheck,
  listBidsByBidder,
  listDocumentsByBid,
  listTenders,
} from "@/lib/api";
import { dateTime, inr, shortDate } from "@/lib/format";
import { documentLabel } from "@/lib/types";

export const Route = createFileRoute("/_portal/officer/bidders/$bidderId")({
  component: BidderReport,
});

function BidderReport() {
  const { bidderId } = Route.useParams();

  const bidder = useQuery({ queryKey: ["bidder", bidderId], queryFn: () => getBidder(bidderId) });
  const bids = useQuery({
    queryKey: ["bids", bidderId],
    queryFn: () => listBidsByBidder(bidderId),
  });
  const tenders = useQuery({ queryKey: ["tenders"], queryFn: listTenders });
  const blacklist = useQuery({
    queryKey: ["blacklist", bidderId],
    queryFn: () => getBlacklistCheck(bidderId),
  });

  const bidList = bids.data ?? [];
  const docQueries = useQueries({
    queries: bidList.map((b) => ({
      queryKey: ["documents", "bid", b.id],
      queryFn: () => listDocumentsByBid(b.id),
    })),
  });

  const tenderTitle = (id: string) => tenders.data?.find((t) => t.id === id)?.title ?? id;

  return (
    <PortalShell title="Bidder report" subtitle={bidder.data?.companyName ?? bidderId}>
      <Button asChild variant="ghost" size="sm" className="mb-4 -ml-2">
        <Link to="/officer/bidders">
          <ArrowLeft className="mr-1 size-4" /> Back to bidders
        </Link>
      </Button>

      {bidder.isLoading ? (
        <LoadingRows rows={3} />
      ) : bidder.isError || !bidder.data ? (
        <ErrorState message="This bidder could not be loaded." />
      ) : (
        <div className="space-y-5">
          <Card>
            <CardContent className="p-6">
              <h2 className="text-xl font-bold">{bidder.data.companyName}</h2>
              <dl className="mt-4 grid gap-4 sm:grid-cols-3">
                <div>
                  <dt className="text-xs text-muted-foreground">Email</dt>
                  <dd className="text-sm font-medium">{bidder.data.email}</dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Phone</dt>
                  <dd className="text-sm font-medium">{bidder.data.phone}</dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Registered</dt>
                  <dd className="text-sm font-medium">{shortDate(bidder.data.createdAt)}</dd>
                </div>
              </dl>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <ShieldAlert className="size-4" /> Blacklist check
              </CardTitle>
            </CardHeader>
            <CardContent>
              {blacklist.isLoading ? (
                <p className="text-sm text-muted-foreground">Checking the registry…</p>
              ) : !blacklist.data ? (
                <p className="text-sm text-muted-foreground">
                  Blacklist check pending — the verification service has not responded yet.
                </p>
              ) : (
                <div className="flex flex-wrap items-center gap-3">
                  <StatusBadge status={blacklist.data.status} />
                  <span className="text-sm text-muted-foreground">
                    {blacklist.data.message ?? "No adverse entry found."} Checked{" "}
                    {dateTime(blacklist.data.checkedAt)} via {blacklist.data.provider}.
                  </span>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Bids and their documents</CardTitle>
            </CardHeader>
            <CardContent className="space-y-5">
              {bids.isLoading ? (
                <LoadingRows rows={2} />
              ) : bidList.length === 0 ? (
                <EmptyState
                  title="No bids from this company"
                  description="Documents are attached to bids, so none exist yet."
                />
              ) : (
                bidList.map((b, i) => {
                  const docs = docQueries[i]?.data ?? [];
                  return (
                    <div key={b.id} className="rounded-md border p-4">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <div>
                          <p className="font-medium">{tenderTitle(b.tenderId)}</p>
                          <p className="text-xs text-muted-foreground">
                            {b.reference} · {b.quote ? inr(b.quote) : "no quote"}
                          </p>
                        </div>
                        <StatusBadge status={b.status} />
                      </div>

                      {docQueries[i]?.isLoading ? (
                        <p className="mt-3 text-sm text-muted-foreground">Loading documents…</p>
                      ) : docs.length === 0 ? (
                        <p className="mt-3 text-sm text-muted-foreground">
                          No documents attached to this bid.
                        </p>
                      ) : (
                        <div className="mt-4 space-y-4">
                          {docs.map((d) => (
                            <div key={d.id} className="rounded-md border bg-surface/50 p-3">
                              <div className="flex flex-wrap items-center justify-between gap-2">
                                <div>
                                  <p className="text-sm font-medium">
                                    {documentLabel(d.documentType)}
                                  </p>
                                  <p className="text-xs text-muted-foreground">
                                    {d.fileName} · {dateTime(d.uploadedAt)}
                                  </p>
                                </div>
                                <StatusBadge status={d.status} />
                              </div>
                              <DocumentIntelligencePanel documentId={d.id} />
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </PortalShell>
  );
}
