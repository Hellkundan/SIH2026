import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { Trash2 } from "lucide-react";
import { toast } from "sonner";

import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
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
import { deleteBidder, listBidders } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { shortDate } from "@/lib/format";
import { isAdminRole } from "@/lib/types";

export const Route = createFileRoute("/_portal/officer/bidders/")({
  component: BiddersList,
});

function BiddersList() {
  const { user } = useAuth();
  const admin = isAdminRole(user?.role);
  const qc = useQueryClient();

  const bidders = useQuery({ queryKey: ["bidders"], queryFn: listBidders });

  const remove = useMutation({
    mutationFn: (id: string) => deleteBidder(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bidders"] });
      toast.success("Bidder removed");
    },
    onError: () => toast.error("Only administrators can remove bidders."),
  });

  const list = bidders.data ?? [];

  return (
    <PortalShell
      title="Registered bidders"
      subtitle="Open a company to see its verification report"
    >
      <Card>
        <CardContent className="p-0">
          {bidders.isLoading ? (
            <div className="p-5">
              <LoadingRows rows={3} />
            </div>
          ) : bidders.isError ? (
            <div className="p-5">
              <ErrorState />
            </div>
          ) : list.length === 0 ? (
            <div className="p-5">
              <EmptyState
                title="No bidders registered"
                description="Companies appear here once their accounts are created."
              />
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Company</TableHead>
                  <TableHead>Contact</TableHead>
                  <TableHead>Registered</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.map((b) => (
                  <TableRow key={b.id}>
                    <TableCell>
                      <p className="font-medium">{b.companyName}</p>
                      <p className="text-xs text-muted-foreground">{b.id}</p>
                    </TableCell>
                    <TableCell className="text-sm">
                      <p>{b.email}</p>
                      <p className="text-muted-foreground">{b.phone}</p>
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {shortDate(b.createdAt)}
                    </TableCell>
                    <TableCell>
                      <div className="flex justify-end gap-2">
                        <Button asChild size="sm" variant="outline">
                          <Link to="/officer/bidders/$bidderId" params={{ bidderId: b.id }}>
                            View report
                          </Link>
                        </Button>
                        {admin ? (
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button size="sm" variant="ghost" aria-label="Remove bidder">
                                <Trash2 className="size-4" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Remove this bidder?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  {b.companyName} will lose access to the portal. This cannot be
                                  undone.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Cancel</AlertDialogCancel>
                                <AlertDialogAction onClick={() => remove.mutate(b.id)}>
                                  Remove
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        ) : null}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {!admin ? (
        <p className="mt-3 text-xs text-muted-foreground">
          Removing bidders is restricted to administrator accounts.
        </p>
      ) : null}
    </PortalShell>
  );
}
