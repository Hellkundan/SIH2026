import { Link, createFileRoute } from "@tanstack/react-router";
import { CheckCircle2 } from "lucide-react";
import { useState } from "react";

import { Logo } from "@/components/Logo";
import { ModeBadge } from "@/components/ModeBadge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { registerBidder } from "@/lib/api";
import { ApiError } from "@/lib/api/client";

export const Route = createFileRoute("/register")({
  head: () => ({
    meta: [
      { title: "Request a Bidder Account — DIXY" },
      {
        name: "description",
        content:
          "Request a DIXY bidder account. A procurement officer reviews every registration request and issues your sign-in details.",
      },
      { property: "og:title", content: "Request a bidder account on DIXY" },
      {
        property: "og:description",
        content: "Vendor onboarding for automated tender compliance verification.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: RegisterPage,
});

type Result =
  | { kind: "created"; company: string }
  | { kind: "pending" }
  | { kind: "error"; message: string };

function RegisterPage() {
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState<Result | null>(null);
  const [form, setForm] = useState({ companyName: "", email: "", phone: "" });

  const set = (key: keyof typeof form) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setResult(null);
    try {
      const bidder = await registerBidder(form);
      // No auto-login: sign-in credentials are issued separately.
      setResult({ kind: "created", company: bidder.companyName });
    } catch (err) {
      if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
        setResult({ kind: "pending" });
      } else {
        setResult({
          kind: "error",
          message: "We couldn't send your request just now. Please try again in a moment.",
        });
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="min-h-screen bg-surface">
      <div className="mx-auto w-full max-w-2xl px-4 py-12">
        <div className="flex items-center justify-between">
          <Logo />
          <ModeBadge />
        </div>
        <div className="mt-8 rounded-xl border bg-card p-6 md:p-8">
          <h1 className="text-2xl font-bold">Request a bidder account</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            New companies are added by a procurement officer. Send your details and you'll receive
            sign-in credentials by email.
          </p>

          {result?.kind === "created" ? (
            <div className="mt-8 rounded-lg border border-success/40 bg-success/8 p-6 text-center">
              <CheckCircle2 className="mx-auto size-10 text-success" />
              <h2 className="mt-3 text-lg font-semibold">{result.company} has been registered</h2>
              <p className="mt-1 text-sm text-muted-foreground">
                Sign-in details are issued separately — you are not signed in yet.
              </p>
              <Button asChild className="mt-5">
                <Link to="/login">Go to sign in</Link>
              </Button>
            </div>
          ) : (
            <>
              {result?.kind === "pending" ? (
                <p className="mt-6 rounded-md border border-info/40 bg-info/8 px-3 py-3 text-sm">
                  Registration requests are reviewed by a procurement officer — you'll get sign-in
                  details by email once your company is approved.
                </p>
              ) : null}
              {result?.kind === "error" ? (
                <p className="mt-6 rounded-md border border-destructive/30 bg-destructive/5 px-3 py-3 text-sm text-destructive">
                  {result.message}
                </p>
              ) : null}

              <form onSubmit={onSubmit} className="mt-8 grid gap-5 md:grid-cols-2">
                <div className="space-y-2 md:col-span-2">
                  <Label htmlFor="companyName">Company name</Label>
                  <Input
                    id="companyName"
                    required
                    value={form.companyName}
                    onChange={set("companyName")}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input id="email" type="email" required value={form.email} onChange={set("email")} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Phone</Label>
                  <Input id="phone" required value={form.phone} onChange={set("phone")} placeholder="+91" />
                </div>
                <div className="md:col-span-2">
                  <Button type="submit" className="w-full" disabled={busy}>
                    {busy ? "Sending request…" : "Send registration request"}
                  </Button>
                </div>
              </form>
            </>
          )}

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Already registered?{" "}
            <Link to="/login" className="font-semibold text-accent hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
