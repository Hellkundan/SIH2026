import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { ListChecks, Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";

import { EmptyState, ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { StatusBadge } from "@/components/StatusBadge";
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Textarea } from "@/components/ui/textarea";
import {
  createTender,
  deleteTender,
  listAllBids,
  listTenders,
  setTenderStatus,
  updateTender,
} from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { shortDate } from "@/lib/format";
import { isAdminRole } from "@/lib/types";
import type { Tender } from "@/lib/types";

export const Route = createFileRoute("/_portal/officer/tenders/")({
  component: ManageTenders,
});

function ManageTenders() {
  const { user } = useAuth();
  const admin = isAdminRole(user?.role);
  const qc = useQueryClient();

  const tenders = useQuery({ queryKey: ["tenders"], queryFn: listTenders });
  const bids = useQuery({ queryKey: ["bids", "all"], queryFn: listAllBids });

  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Tender | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const refresh = () => qc.invalidateQueries({ queryKey: ["tenders"] });

  const save = useMutation({
    mutationFn: () =>
      editing
        ? updateTender(editing.id, { title, description })
        : createTender({ title, description }),
    onSuccess: () => {
      refresh();
      setOpen(false);
      toast.success(editing ? "Tender updated" : "Tender created as draft");
    },
    onError: () => toast.error("The tender could not be saved."),
  });

  const status = useMutation({
    mutationFn: ({ id, action }: { id: string; action: "open" | "close" }) =>
      setTenderStatus(id, action),
    onSuccess: (t) => {
      refresh();
      toast.success(t.status === "OPEN" ? "Tender published" : "Tender closed");
    },
    onError: () => toast.error("The status could not be changed."),
  });

  const remove = useMutation({
    mutationFn: (id: string) => deleteTender(id),
    onSuccess: () => {
      refresh();
      toast.success("Tender deleted");
    },
    onError: () => toast.error("Only administrators can delete tenders."),
  });

  const startCreate = () => {
    setEditing(null);
    setTitle("");
    setDescription("");
    setOpen(true);
  };

  const startEdit = (t: Tender) => {
    setEditing(t);
    setTitle(t.title);
    setDescription(t.description);
    setOpen(true);
  };

  const list = tenders.data ?? [];

  return (
    <PortalShell
      title="Manage tenders"
      subtitle="Create, publish and close tenders"
      actions={
        <Button onClick={startCreate}>
          <Plus className="mr-1 size-4" /> New tender
        </Button>
      }
    >
      <Card>
        <CardContent className="p-0">
          {tenders.isLoading ? (
            <div className="p-5">
              <LoadingRows rows={4} />
            </div>
          ) : tenders.isError ? (
            <div className="p-5">
              <ErrorState />
            </div>
          ) : list.length === 0 ? (
            <div className="p-5">
              <EmptyState
                title="No tenders yet"
                description="Create your first tender to start receiving bids."
                action={<Button onClick={startCreate}>New tender</Button>}
              />
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tender</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Bids</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.map((t) => (
                  <TableRow key={t.id}>
                    <TableCell className="max-w-80">
                      <button
                        className="text-left font-medium hover:underline"
                        onClick={() => startEdit(t)}
                      >
                        {t.title}
                      </button>
                      <p className="line-clamp-1 text-xs text-muted-foreground">{t.description}</p>
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {shortDate(t.createdAt)}
                    </TableCell>
                    <TableCell>
                      {(bids.data ?? []).filter((b) => b.tenderId === t.id).length}
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={t.status} />
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap justify-end gap-2">
                        <Button asChild size="sm" variant="outline">
                          <Link
                            to="/officer/tenders/$tenderId/requirements"
                            params={{ tenderId: t.id }}
                          >
                            <ListChecks className="mr-1 size-4" /> Requirements
                          </Link>
                        </Button>
                        <Button asChild size="sm" variant="outline">
                          <Link to="/officer/tenders/$tenderId/bids" params={{ tenderId: t.id }}>
                            Bids
                          </Link>
                        </Button>
                        {t.status === "OPEN" ? (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => status.mutate({ id: t.id, action: "close" })}
                          >
                            Close
                          </Button>
                        ) : (
                          <Button
                            size="sm"
                            onClick={() => status.mutate({ id: t.id, action: "open" })}
                          >
                            Publish
                          </Button>
                        )}
                        {admin ? (
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button size="sm" variant="ghost" aria-label="Delete tender">
                                <Trash2 className="size-4" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Delete this tender?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  “{t.title}” and its requirements will be removed. This cannot be
                                  undone.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Cancel</AlertDialogCancel>
                                <AlertDialogAction onClick={() => remove.mutate(t.id)}>
                                  Delete
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
          Deleting tenders is restricted to administrator accounts.
        </p>
      ) : null}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? "Edit tender" : "New tender"}</DialogTitle>
            <DialogDescription>
              A tender needs a title and a description. Everything else lives in its requirements
              list.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="title">Title</Label>
              <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                rows={6}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button onClick={() => save.mutate()} disabled={!title.trim() || save.isPending}>
              {save.isPending ? "Saving…" : editing ? "Save changes" : "Create tender"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </PortalShell>
  );
}
