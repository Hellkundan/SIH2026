import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, createFileRoute } from "@tanstack/react-router";
import { ArrowLeft, GripVertical, Plus, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";

import { ErrorState, LoadingRows } from "@/components/EmptyState";
import { PortalShell } from "@/components/PortalShell";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { getTender, listRequirements, saveRequirements } from "@/lib/api";

export const Route = createFileRoute("/_portal/officer/tenders/$tenderId/requirements")({
  component: RequirementsBuilder,
});

const QUICK_INSERTS = [
  "Valid GST Registration",
  "PAN Card of the bidding entity",
  "Udyam / MSME Certificate",
  "EPFO Registration",
  "ESIC Registration",
  "NSIC Registration",
  "OEM Authorization Letter",
  "Startup India Recognition",
  "Make in India Declaration",
  "Three years of audited financial statements",
];

interface Row {
  requirement: string;
  mandatory: boolean;
}

function RequirementsBuilder() {
  const { tenderId } = Route.useParams();
  const qc = useQueryClient();

  const tender = useQuery({ queryKey: ["tender", tenderId], queryFn: () => getTender(tenderId) });
  const existing = useQuery({
    queryKey: ["requirements", tenderId],
    queryFn: () => listRequirements(tenderId),
  });

  const [rows, setRows] = useState<Row[]>([]);
  const [draft, setDraft] = useState("");
  const [mandatory, setMandatory] = useState(true);

  useEffect(() => {
    if (existing.data) {
      setRows(existing.data.map((r) => ({ requirement: r.requirement, mandatory: r.mandatory })));
    }
  }, [existing.data]);

  const save = useMutation({
    mutationFn: () =>
      saveRequirements(
        tenderId,
        rows.map((r) => ({ tenderId, requirement: r.requirement.trim(), mandatory: r.mandatory })),
      ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["requirements", tenderId] });
      toast.success("Requirements saved");
    },
    onError: () => toast.error("The requirements could not be saved."),
  });

  const add = () => {
    const text = draft.trim();
    if (!text) return;
    setRows((r) => [...r, { requirement: text, mandatory }]);
    setDraft("");
  };

  return (
    <PortalShell title="Requirements" subtitle={tender.data?.title ?? tenderId}>
      <Button asChild variant="ghost" size="sm" className="mb-4 -ml-2">
        <Link to="/officer/tenders">
          <ArrowLeft className="mr-1 size-4" /> Back to tenders
        </Link>
      </Button>

      {existing.isError ? (
        <ErrorState message="Existing requirements could not be loaded." />
      ) : null}

      <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Add a requirement</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="requirement">Requirement</Label>
              <Input
                id="requirement"
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    add();
                  }
                }}
                placeholder="e.g. Valid GST Registration in the bidder's own name"
              />
            </div>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <label className="flex items-center gap-2 text-sm">
                <Switch checked={mandatory} onCheckedChange={setMandatory} />
                {mandatory ? "Mandatory" : "Preferred"}
              </label>
              <Button onClick={add} disabled={!draft.trim()}>
                <Plus className="mr-1 size-4" /> Add requirement
              </Button>
            </div>

            <div>
              <p className="text-xs font-semibold text-muted-foreground">Quick inserts</p>
              <div className="mt-2 flex flex-wrap gap-2">
                {QUICK_INSERTS.map((q) => (
                  <button
                    key={q}
                    type="button"
                    onClick={() => setDraft(q)}
                    className="rounded-full border bg-background px-3 py-1 text-xs hover:border-accent hover:text-accent"
                  >
                    {q}
                  </button>
                ))}
              </div>
              <p className="mt-2 text-xs text-muted-foreground">
                Chips only fill the box — everything is saved as plain text.
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              Requirement list {rows.length ? `(${rows.length})` : ""}
            </CardTitle>
          </CardHeader>
          <CardContent>
            {existing.isLoading ? (
              <LoadingRows rows={3} />
            ) : rows.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Nothing added yet. Bidders will see “No requirements listed”.
              </p>
            ) : (
              <ul className="space-y-2">
                {rows.map((r, i) => (
                  <li
                    key={`${r.requirement}-${i}`}
                    className="flex items-start gap-2 rounded-md border bg-background p-2.5"
                  >
                    <GripVertical className="mt-2 size-4 shrink-0 text-muted-foreground" />
                    <div className="min-w-0 flex-1 space-y-2">
                      <Input
                        value={r.requirement}
                        onChange={(e) =>
                          setRows((prev) =>
                            prev.map((row, idx) =>
                              idx === i ? { ...row, requirement: e.target.value } : row,
                            ),
                          )
                        }
                      />
                      <label className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Switch
                          checked={r.mandatory}
                          onCheckedChange={(v) =>
                            setRows((prev) =>
                              prev.map((row, idx) => (idx === i ? { ...row, mandatory: v } : row)),
                            )
                          }
                        />
                        {r.mandatory ? "Mandatory" : "Preferred"}
                      </label>
                    </div>
                    <Button
                      size="sm"
                      variant="ghost"
                      aria-label="Remove requirement"
                      onClick={() => setRows((prev) => prev.filter((_, idx) => idx !== i))}
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  </li>
                ))}
              </ul>
            )}

            <Button
              className="mt-5 w-full"
              onClick={() => save.mutate()}
              disabled={save.isPending || rows.some((r) => !r.requirement.trim())}
            >
              {save.isPending ? "Saving…" : "Save requirements"}
            </Button>
          </CardContent>
        </Card>
      </div>
    </PortalShell>
  );
}
