import { useQuery } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { ArrowLeft, CircleDot, UploadCloud } from "lucide-react";

import { ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { getTender, listRequirements } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { shortDate } from "@/lib/format";
import { isOfficerRole } from "@/lib/types";

export const Route = createFileRoute("/_portal/tenders/$tenderId")({
  component: TenderDetail,
});

function TenderDetail() {
  const { tenderId } = Route.useParams();
  const { user } = useAuth();
  const officer = isOfficerRole(user?.role);

  const tender = useQuery({ queryKey: ["tender", tenderId], queryFn: () => getTender(tenderId) });
  const reqs = useQuery({
    queryKey: ["requirements", tenderId],
    queryFn: () => listRequirements(tenderId),
  });

  const list = reqs.data ?? [];
  const mandatory = list.filter((r) => r.mandatory);
  const canBid = !officer && tender.data?.status === "OPEN";

  return (
    <PortalShell title="Tender details" subtitle={tenderId}>
      <Button asChild variant="ghost" size="sm" className="mb-4 -ml-2">
        <Link to="/tenders">
          <ArrowLeft className="mr-1 size-4" /> Back to tenders
        </Link>
      </Button>

      {tender.isLoading ? (
        <LoadingRows rows={4} />
      ) : tender.isError || !tender.data ? (
        <ErrorState message="This tender could not be loaded." />
      ) : (
        <div className="grid gap-5 lg:grid-cols-[1.6fr_1fr]">
          <div className="space-y-5">
            <Card>
              <CardContent className="p-6">
                <StatusBadge status={tender.data.status} />
                <h2 className="mt-3 text-2xl font-bold">{tender.data.title}</h2>
                <p className="mt-5 text-sm leading-relaxed whitespace-pre-line">
                  {tender.data.description}
                </p>
                <dl className="mt-6 grid gap-4 border-t pt-5 sm:grid-cols-2">
                  <div>
                    <dt className="text-xs text-muted-foreground">Published</dt>
                    <dd className="text-sm font-medium">{shortDate(tender.data.createdAt)}</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-muted-foreground">Reference</dt>
                    <dd className="text-sm font-medium">{tender.data.id}</dd>
                  </div>
                </dl>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">Requirements</CardTitle>
              </CardHeader>
              <CardContent>
                {reqs.isLoading ? (
                  <LoadingRows rows={3} />
                ) : reqs.isError ? (
                  <ErrorState message="Requirements could not be loaded." />
                ) : list.length === 0 ? (
                  <p className="text-sm text-muted-foreground">
                    No requirements have been published for this tender yet.
                  </p>
                ) : (
                  <ul className="space-y-2">
                    {list.map((r) => (
                      <li
                        key={r.id}
                        className="flex items-start gap-3 rounded-md border bg-background px-3 py-3"
                      >
                        <CircleDot
                          className={`mt-0.5 size-4 shrink-0 ${
                            r.mandatory ? "text-primary" : "text-muted-foreground"
                          }`}
                        />
                        <p className="text-sm">
                          {r.requirement}{" "}
                          <span className="text-xs text-muted-foreground">
                            {r.mandatory ? "(mandatory)" : "(preferred)"}
                          </span>
                        </p>
                      </li>
                    ))}
                  </ul>
                )}
              </CardContent>
            </Card>
          </div>

          <div className="space-y-5">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">What you'll need</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  {list.length === 0
                    ? "No documents have been requested yet — you can still start a bid."
                    : `${mandatory.length} mandatory and ${
                        list.length - mandatory.length
                      } preferred requirement(s). You attach supporting documents to the bid itself.`}
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="space-y-3 p-6">
                {canBid ? (
                  <>
                    <Button asChild size="lg" className="w-full">
                      <Link to="/bid/$tenderId" params={{ tenderId }}>
                        Start bid
                      </Link>
                    </Button>
                    <Button asChild variant="outline" size="lg" className="w-full">
                      <Link to="/bid/$tenderId" params={{ tenderId }}>
                        <UploadCloud className="mr-1 size-4" /> Upload documents
                      </Link>
                    </Button>
                    <p className="text-center text-xs text-muted-foreground">
                      Both open your draft bid — documents are attached to it.
                    </p>
                  </>
                ) : (
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <span className="block">
                        <Button size="lg" className="w-full" disabled>
                          Start bid
                        </Button>
                      </span>
                    </TooltipTrigger>
                    <TooltipContent>
                      {officer
                        ? "Officers review bids rather than submit them."
                        : "This tender is not open for bidding."}
                    </TooltipContent>
                  </Tooltip>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </PortalShell>
  );
}
