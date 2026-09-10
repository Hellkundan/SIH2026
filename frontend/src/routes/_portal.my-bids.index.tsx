import { useQuery } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";

import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { listBidsByBidder, listTenders } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { dateTime, inr } from "@/lib/format";

export const Route = createFileRoute("/_portal/my-bids/")({
  component: MyBids,
});

function MyBids() {
  const { bidder } = useAuth();
  const bidderId = bidder?.id ?? "";
  const bids = useQuery({
    queryKey: ["bids", bidderId],
    queryFn: () => listBidsByBidder(bidderId),
    enabled: !!bidderId,
  });
  const tenders = useQuery({ queryKey: ["tenders"], queryFn: listTenders });

  return (
    <PortalShell title="My bids" subtitle="Every bid you have started or submitted">
      <Card>
        <CardContent className="p-0">
          {bids.isLoading ? (
            <div className="p-5">
              <LoadingRows rows={4} />
            </div>
          ) : bids.isError ? (
            <div className="p-5">
              <ErrorState />
            </div>
          ) : (bids.data ?? []).length === 0 ? (
            <div className="p-5">
              <EmptyState
                title="You haven't bid yet"
                description="Find an open tender and start your first bid."
                action={
                  <Button asChild>
                    <Link to="/tenders">Browse tenders</Link>
                  </Button>
                }
              />
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tender</TableHead>
                  <TableHead>Reference</TableHead>
                  <TableHead>Quote</TableHead>
                  <TableHead>Submitted</TableHead>
                  <TableHead>Last updated</TableHead>
                  <TableHead className="text-right">Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(bids.data ?? []).map((bid) => (
                  <TableRow key={bid.id}>
                    <TableCell className="max-w-72">
                      <Link
                        to="/my-bids/$bidId"
                        params={{ bidId: bid.id }}
                        className="font-medium hover:underline"
                      >
                        {tenders.data?.find((t) => t.id === bid.tenderId)?.title ?? bid.tenderId}
                      </Link>
                    </TableCell>
                    <TableCell className="text-xs text-muted-foreground">{bid.reference}</TableCell>
                    <TableCell>{bid.quote ? inr(bid.quote) : "—"}</TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {bid.submittedAt ? dateTime(bid.submittedAt) : "Not submitted"}
                    </TableCell>
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
