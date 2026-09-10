import { useQuery } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { ListChecks, Search } from "lucide-react";
import { useState } from "react";

import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { listRequirements, listTenders } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { shortDate } from "@/lib/format";
import { isOfficerRole } from "@/lib/types";

export const Route = createFileRoute("/_portal/tenders/")({
  component: BrowseTenders,
});

function BrowseTenders() {
  const { user } = useAuth();
  const officer = isOfficerRole(user?.role);

  const tenders = useQuery({ queryKey: ["tenders"], queryFn: listTenders });

  const [q, setQ] = useState("");
  const [status, setStatus] = useState("OPEN");

  const filtered = (tenders.data ?? []).filter((t) => {
    const matchQ =
      !q ||
      t.title.toLowerCase().includes(q.toLowerCase()) ||
      t.description.toLowerCase().includes(q.toLowerCase());
    return matchQ && (status === "ALL" || t.status === status);
  });

  return (
    <PortalShell
      title="Browse tenders"
      subtitle={officer ? "Read-only view of published tenders" : "Tenders you can bid on"}
    >
      <Card>
        <CardContent className="grid gap-3 p-4 md:grid-cols-[1fr_180px]">
          <div className="relative">
            <Search className="absolute top-2.5 left-3 size-4 text-muted-foreground" />
            <Input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Search by keyword"
              className="pl-9"
            />
          </div>
          <Select value={status} onValueChange={setStatus}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All statuses</SelectItem>
              <SelectItem value="OPEN">Open</SelectItem>
              <SelectItem value="CLOSED">Closed</SelectItem>
              <SelectItem value="DRAFT">Draft</SelectItem>
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      <div className="mt-5">
        {tenders.isLoading ? (
          <LoadingRows rows={3} />
        ) : tenders.isError ? (
          <ErrorState />
        ) : filtered.length === 0 ? (
          <EmptyState
            title="No tenders match those filters"
            description="Try clearing the keyword or widening the status filter."
          />
        ) : (
          <div className="grid gap-4">
            {filtered.map((t) => (
              <Card key={t.id}>
                <CardContent className="p-5">
                  <Link
                    to="/tenders/$tenderId"
                    params={{ tenderId: t.id }}
                    className="text-lg font-semibold hover:underline"
                  >
                    {t.title}
                  </Link>
                  <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                    <StatusBadge status={t.status} />
                    <span>{t.id}</span>
                    <span>·</span>
                    <span>Published {shortDate(t.createdAt)}</span>
                  </div>
                  <p className="mt-3 line-clamp-2 text-sm text-muted-foreground">{t.description}</p>
                  <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
                    <RequirementCount tenderId={t.id} />
                    <Button asChild size="sm">
                      <Link to="/tenders/$tenderId" params={{ tenderId: t.id }}>
                        View details
                      </Link>
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </PortalShell>
  );
}

function RequirementCount({ tenderId }: { tenderId: string }) {
  const reqs = useQuery({
    queryKey: ["requirements", tenderId],
    queryFn: () => listRequirements(tenderId),
  });
  const list = reqs.data ?? [];
  const mandatory = list.filter((r) => r.mandatory).length;

  return (
    <span className="inline-flex items-center gap-1.5 text-sm text-muted-foreground">
      <ListChecks className="size-4" />
      {reqs.isLoading
        ? "Loading requirements…"
        : list.length === 0
          ? "No requirements listed"
          : `${list.length} requirement${list.length === 1 ? "" : "s"} · ${mandatory} mandatory`}
    </span>
  );
}
