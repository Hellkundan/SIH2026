import { useQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { Building2, Search } from "lucide-react";
import { useState } from "react";

import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { Logo } from "@/components/Logo";
import { ModeBadge } from "@/components/ModeBadge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { listBidders } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { isOfficerRole } from "@/lib/types";

export const Route = createFileRoute("/_portal/select-company")({
  component: SelectCompany,
});

/**
 * Sign-in never tells us which company the user bids for, so they pick it once
 * and we remember it on this device.
 */
function SelectCompany() {
  const { user, bidder, setBidder } = useAuth();
  const navigate = useNavigate();
  const [q, setQ] = useState("");

  const bidders = useQuery({ queryKey: ["bidders"], queryFn: listBidders });

  const rows = (bidders.data ?? []).filter(
    (b) => !q || b.companyName.toLowerCase().includes(q.toLowerCase()),
  );

  const choose = (id: string, companyName: string) => {
    setBidder({ id, companyName });
    navigate({ to: isOfficerRole(user?.role) ? "/officer" : "/dashboard", replace: true });
  };

  return (
    <div className="min-h-screen bg-surface">
      <div className="mx-auto w-full max-w-2xl px-4 py-12">
        <div className="flex items-center justify-between">
          <Logo />
          <ModeBadge />
        </div>

        <div className="mt-8 rounded-xl border bg-card p-6 md:p-8">
          <h1 className="text-2xl font-bold">Which company are you bidding for?</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Signed in as <span className="font-medium text-foreground">{user?.username}</span>. Pick
            your company once — every bid, document and dashboard uses it from here.
          </p>

          <div className="relative mt-6">
            <Search className="absolute top-2.5 left-3 size-4 text-muted-foreground" />
            <Input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Search companies"
              className="pl-9"
            />
          </div>

          <div className="mt-4 space-y-2">
            {bidders.isLoading ? (
              <LoadingRows rows={4} />
            ) : bidders.isError ? (
              <ErrorState message="The company list could not be loaded." />
            ) : rows.length === 0 ? (
              <EmptyState
                title="No matching company"
                description="Ask a procurement officer to register your company if it isn't listed."
              />
            ) : (
              rows.map((b) => (
                <button
                  key={b.id}
                  onClick={() => choose(b.id, b.companyName)}
                  className={`flex w-full items-center gap-3 rounded-md border p-3 text-left transition-colors hover:bg-muted ${
                    bidder?.id === b.id ? "border-primary bg-surface" : ""
                  }`}
                >
                  <span className="grid size-9 shrink-0 place-items-center rounded-md bg-primary/10 text-primary">
                    <Building2 className="size-4" />
                  </span>
                  <span className="min-w-0">
                    <span className="block truncate text-sm font-semibold">{b.companyName}</span>
                    <span className="block truncate text-xs text-muted-foreground">
                      {b.email} · {b.phone}
                    </span>
                  </span>
                </button>
              ))
            )}
          </div>

          {bidder ? (
            <Button
              variant="ghost"
              className="mt-4"
              onClick={() => navigate({ to: "/dashboard", replace: true })}
            >
              Keep {bidder.companyName}
            </Button>
          ) : null}
        </div>
      </div>
    </div>
  );
}
