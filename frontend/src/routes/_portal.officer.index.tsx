import { useQuery } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import {
  Bar,
  BarChart,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip as RTooltip,
  XAxis,
  YAxis,
} from "recharts";

import { EmptyState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { listAllBids, listBidders, listTenders } from "@/lib/api";
import { dateTime, inr, shortDate, titleCase } from "@/lib/format";

export const Route = createFileRoute("/_portal/officer/")({
  component: OfficerDashboard,
});

const COLORS = ["#2a3f6b", "#3a9aa8", "#3f8f63", "#c58a2a", "#b4462f"];

function OfficerDashboard() {
  const tenders = useQuery({ queryKey: ["tenders"], queryFn: listTenders });
  const bids = useQuery({ queryKey: ["bids", "all"], queryFn: listAllBids });
  const bidders = useQuery({ queryKey: ["bidders"], queryFn: listBidders });

  const allBids = bids.data ?? [];
  const activeTenders = (tenders.data ?? []).filter((t) => t.status === "OPEN").length;
  const pending = allBids.filter((b) => b.status === "SUBMITTED" || b.status === "UNDER_REVIEW");
  const decided = allBids.filter((b) => b.status === "QUALIFIED" || b.status === "DISQUALIFIED");
  const passRate = decided.length
    ? Math.round((decided.filter((b) => b.status === "QUALIFIED").length / decided.length) * 100)
    : 0;

  const bidsByStatus = ["DRAFT", "SUBMITTED", "UNDER_REVIEW", "QUALIFIED", "DISQUALIFIED"]
    .map((s) => ({ name: titleCase(s), value: allBids.filter((b) => b.status === s).length }))
    .filter((d) => d.value > 0);

  const tendersByStatus = ["DRAFT", "OPEN", "CLOSED"].map((s) => ({
    name: titleCase(s),
    count: (tenders.data ?? []).filter((t) => t.status === s).length,
  }));

  const recentTenders = [...(tenders.data ?? [])]
    .sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt))
    .slice(0, 6);

  return (
    <PortalShell
      title="Procurement dashboard"
      subtitle="Officer portal"
      actions={
        <Button asChild size="sm">
          <Link to="/officer/tenders">Manage tenders</Link>
        </Button>
      }
    >
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[
          ["Active tenders", activeTenders],
          ["Total bids received", allBids.length],
          ["Bids pending review", pending.length],
          ["Qualification rate", `${passRate}%`],
        ].map(([label, value]) => (
          <Card key={String(label)}>
            <CardContent className="p-5">
              <p className="text-sm text-muted-foreground">{label}</p>
              <p className="mt-1 text-3xl font-extrabold text-primary">{value}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="mt-5 grid gap-5 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Bids by status</CardTitle>
          </CardHeader>
          <CardContent className="h-64">
            {bids.isLoading ? (
              <LoadingRows rows={2} />
            ) : bidsByStatus.length === 0 ? (
              <EmptyState title="No bids yet" description="Charts appear once bids arrive." />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={bidsByStatus} dataKey="value" nameKey="name" innerRadius={55} label>
                    {bidsByStatus.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <RTooltip />
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Tenders by status</CardTitle>
          </CardHeader>
          <CardContent className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={tendersByStatus}>
                <XAxis dataKey="name" tickLine={false} axisLine={false} fontSize={12} />
                <YAxis allowDecimals={false} tickLine={false} axisLine={false} fontSize={12} />
                <RTooltip />
                <Bar dataKey="count" fill="#2a3f6b" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      <div className="mt-5 grid gap-5 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Bids awaiting review</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            {bids.isLoading ? (
              <div className="p-5">
                <LoadingRows rows={3} />
              </div>
            ) : pending.length === 0 ? (
              <div className="p-5">
                <EmptyState title="Nothing waiting" description="All submitted bids are decided." />
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Bidder</TableHead>
                    <TableHead>Quote</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Action</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {pending.map((b) => (
                    <TableRow key={b.id}>
                      <TableCell>
                        <p className="text-sm font-medium">
                          {bidders.data?.find((x) => x.id === b.bidderId)?.companyName ??
                            b.bidderId}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {b.submittedAt ? dateTime(b.submittedAt) : "—"}
                        </p>
                      </TableCell>
                      <TableCell>{b.quote ? inr(b.quote) : "—"}</TableCell>
                      <TableCell>
                        <StatusBadge status={b.status} />
                      </TableCell>
                      <TableCell className="text-right">
                        <Button asChild size="sm" variant="outline">
                          <Link
                            to="/officer/tenders/$tenderId/bids"
                            params={{ tenderId: b.tenderId }}
                          >
                            Review
                          </Link>
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Recently created tenders</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            {tenders.isLoading ? (
              <div className="p-5">
                <LoadingRows rows={3} />
              </div>
            ) : recentTenders.length === 0 ? (
              <div className="p-5">
                <EmptyState title="No tenders yet" description="Create your first tender." />
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tender</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead className="text-right">Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {recentTenders.map((t) => (
                    <TableRow key={t.id}>
                      <TableCell className="max-w-72 truncate text-sm font-medium">
                        {t.title}
                      </TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {shortDate(t.createdAt)}
                      </TableCell>
                      <TableCell className="text-right">
                        <StatusBadge status={t.status} />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </PortalShell>
  );
}
