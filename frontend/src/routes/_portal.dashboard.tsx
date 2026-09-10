import { useQueries, useQuery } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { FileStack, FileUp, ScrollText } from "lucide-react";

import { DocumentCheckBadge } from "@/components/DocumentIntelligencePanel";
import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { listActivity, listBidsByBidder, listDocumentsByBid, listTenders } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { dateTime, inr } from "@/lib/format";
import { documentLabel } from "@/lib/types";

export const Route = createFileRoute("/_portal/dashboard")({
  component: BidderDashboard,
});

function BidderDashboard() {
  const { user, bidder } = useAuth();
  const bidderId = bidder?.id ?? "";

  const bids = useQuery({
    queryKey: ["bids", bidderId],
    queryFn: () => listBidsByBidder(bidderId),
    enabled: !!bidderId,
  });
  const tenders = useQuery({ queryKey: ["tenders"], queryFn: listTenders });
  const activity = useQuery({ queryKey: ["activity"], queryFn: listActivity });

  const bidList = bids.data ?? [];
  const docQueries = useQueries({
    queries: bidList.map((b) => ({
      queryKey: ["documents", "bid", b.id],
      queryFn: () => listDocumentsByBid(b.id),
    })),
  });

  const allDocs = docQueries.flatMap((q) => q.data ?? []);
  const verified = allDocs.filter((d) => d.status === "VERIFIED").length;
  const total = allDocs.length;
  const tenderTitle = (id: string) => tenders.data?.find((t) => t.id === id)?.title ?? id;

  return (
    <PortalShell
      title={`Welcome, ${bidder?.companyName ?? user?.username ?? "Bidder"}`}
      subtitle="Bidder portal"
    >
      <div className="grid gap-5 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardContent className="flex flex-wrap items-center justify-between gap-5 p-6">
            <div>
              <p className="text-sm text-muted-foreground">Documents verified across your bids</p>
              <p className="mt-1 text-3xl font-extrabold text-primary">
                {verified}/{total || 0} documents
              </p>
              <Progress value={total ? (verified / total) * 100 : 0} className="mt-3 w-64" />
            </div>
            <Button asChild size="lg">
              <Link to="/tenders">
                <ScrollText className="mr-1 size-4" /> Find a tender to bid on
              </Link>
            </Button>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="grid gap-3 p-6">
            <Button asChild variant="outline" className="justify-start">
              <Link to="/tenders">
                <ScrollText className="mr-2 size-4" /> Browse open tenders
              </Link>
            </Button>
            <Button asChild variant="outline" className="justify-start">
              <Link to="/my-bids">
                <FileStack className="mr-2 size-4" /> View my bids
              </Link>
            </Button>
            <Button asChild variant="outline" className="justify-start">
              <Link to="/select-company">
                <FileUp className="mr-2 size-4" /> Change company
              </Link>
            </Button>
          </CardContent>
        </Card>
      </div>

      <div className="mt-5 grid gap-5 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-base">Documents by bid</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {bids.isLoading ? (
              <LoadingRows rows={3} />
            ) : bids.isError ? (
              <ErrorState />
            ) : bidList.length === 0 ? (
              <EmptyState
                title="No documents yet"
                description="Documents are uploaded inside a bid. Start a bid on a tender to attach yours."
                action={
                  <Button asChild>
                    <Link to="/tenders">Browse tenders</Link>
                  </Button>
                }
              />
            ) : (
              bidList.map((bid, i) => {
                const docs = docQueries[i]?.data ?? [];
                return (
                  <div key={bid.id} className="rounded-md border bg-background p-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <Link
                        to="/my-bids/$bidId"
                        params={{ bidId: bid.id }}
                        className="text-sm font-semibold hover:underline"
                      >
                        {tenderTitle(bid.tenderId)}
                      </Link>
                      <StatusBadge status={bid.status} />
                    </div>
                    <p className="text-xs text-muted-foreground">{bid.reference}</p>
                    {docQueries[i]?.isLoading ? (
                      <p className="mt-3 text-xs text-muted-foreground">Loading documents…</p>
                    ) : docs.length === 0 ? (
                      <p className="mt-3 text-xs text-muted-foreground">
                        No documents attached to this bid yet.{" "}
                        <Link
                          to="/bid/$tenderId"
                          params={{ tenderId: bid.tenderId }}
                          className="font-medium text-accent hover:underline"
                        >
                          Upload documents
                        </Link>
                      </p>
                    ) : (
                      <ul className="mt-3 space-y-1.5">
                        {docs.map((d) => (
                          <li
                            key={d.id}
                            className="flex flex-wrap items-center justify-between gap-2 rounded border bg-surface px-3 py-2 text-sm"
                          >
                            <span className="min-w-0 truncate">
                              {documentLabel(d.documentType)}
                              <span className="ml-2 text-xs text-muted-foreground">
                                {d.fileName}
                              </span>
                            </span>
                            <DocumentCheckBadge documentId={d.id} />
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                );
              })
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Recent activity</CardTitle>
          </CardHeader>
          <CardContent>
            {activity.isLoading ? (
              <LoadingRows rows={3} />
            ) : (activity.data ?? []).length === 0 ? (
              <p className="text-sm text-muted-foreground">Nothing has happened yet.</p>
            ) : (
              <ol className="space-y-4">
                {(activity.data ?? []).map((a) => (
                  <li key={a.at + a.label} className="border-l-2 border-accent/40 pl-3">
                    <p className="text-sm">{a.label}</p>
                    <p className="text-xs text-muted-foreground">{dateTime(a.at)}</p>
                  </li>
                ))}
              </ol>
            )}
          </CardContent>
        </Card>
      </div>

      <Card className="mt-5">
        <CardHeader>
          <CardTitle className="text-base">Active bids</CardTitle>
        </CardHeader>
        <CardContent>
          {bids.isLoading ? (
            <LoadingRows rows={3} />
          ) : bids.isError ? (
            <ErrorState />
          ) : bidList.length === 0 ? (
            <EmptyState
              title="No bids yet"
              description="Once you bid on a tender it will show up here."
              action={
                <Button asChild>
                  <Link to="/tenders">Browse tenders</Link>
                </Button>
              }
            />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tender</TableHead>
                  <TableHead>Quote</TableHead>
                  <TableHead>Last updated</TableHead>
                  <TableHead className="text-right">Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {bidList.map((bid) => (
                  <TableRow key={bid.id}>
                    <TableCell className="max-w-80">
                      <Link
                        to="/my-bids/$bidId"
                        params={{ bidId: bid.id }}
                        className="font-medium hover:underline"
                      >
                        {tenderTitle(bid.tenderId)}
                      </Link>
                      <p className="text-xs text-muted-foreground">{bid.reference}</p>
                    </TableCell>
                    <TableCell>{bid.quote ? inr(bid.quote) : "—"}</TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {dateTime(bid.updatedAt)}
                    </TableCell>
                    <TableCell className="text-right">
                      <StatusBadge status={bid.status} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </PortalShell>
  );
}
