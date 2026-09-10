import { Link, createFileRoute } from "@tanstack/react-router";
import {
  ArrowRight,
  BadgeCheck,
  FileSearch,
  Gavel,
  ScanLine,
  ShieldAlert,
  Timer,
} from "lucide-react";

import { PublicLayout } from "@/components/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "DIXY — Verified Vendors. Faster Tenders." },
      {
        name: "description",
        content:
          "DIXY verifies bidder documents automatically with OCR and live GST, PAN, Udyam, EPFO, ESIC and blacklist checks, so government tenders move faster.",
      },
      { property: "og:title", content: "DIXY — Verified Vendors. Faster Tenders." },
      {
        property: "og:description",
        content:
          "Automated bidder compliance verification for government e-tendering.",
      },
    ],
  }),
  component: Landing,
});

const features = [
  {
    icon: ScanLine,
    title: "Automated document reading",
    body: "Every certificate is read by OCR the moment it is uploaded — numbers, names and validity dates are extracted with a confidence score, and blurry scans are flagged before an officer ever sees them.",
  },
  {
    icon: BadgeCheck,
    title: "Instant registry checks",
    body: "GST, PAN, Udyam, EPFO, ESIC, NSIC, OEM authorization, Startup India and Make in India declarations are checked against their source registries automatically.",
  },
  {
    icon: Timer,
    title: "Live bid status",
    body: "Bidders watch their bid move from submitted to under review to qualified, with a full timeline and officer remarks at every step.",
  },
  {
    icon: ShieldAlert,
    title: "Blacklist screening",
    body: "Debarred vendors are surfaced at the top of the review screen with the reason and order date, so disqualification decisions are defensible.",
  },
];

const steps = [
  { icon: Gavel, title: "Register", body: "Create your company account in under two minutes." },
  { icon: FileSearch, title: "Upload documents", body: "Drop in your compliance certificates once." },
  { icon: BadgeCheck, title: "Get verified & bid", body: "Checks run automatically; bid on anything you qualify for." },
];

function Landing() {
  return (
    <PublicLayout>
      <section className="relative overflow-hidden bg-sidebar text-sidebar-foreground">
        <div className="pointer-events-none absolute -top-24 -right-24 size-96 rounded-full bg-accent/20 blur-3xl" />
        <div className="mx-auto grid w-full max-w-6xl gap-10 px-4 py-20 md:grid-cols-[1.15fr_1fr] md:py-28">
          <div>
            <span className="inline-flex items-center gap-2 rounded-full border border-sidebar-border px-3 py-1 text-xs font-semibold tracking-wide uppercase">
              Government e-Tendering
            </span>
            <h1 className="mt-5 text-4xl leading-tight font-extrabold md:text-5xl">
              Verified vendors. Faster tenders. Zero paperwork chaos.
            </h1>
            <p className="mt-5 max-w-xl text-base text-sidebar-foreground/75">
              DIXY reads every compliance document a bidder uploads, checks it against the issuing
              government registry, and hands procurement officers a decision-ready compliance score
              instead of a folder of scans.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Button asChild size="lg">
                <Link to="/tenders">
                  Browse tenders <ArrowRight className="ml-1 size-4" />
                </Link>
              </Button>
              <Button
                asChild
                size="lg"
                variant="outline"
                className="border-sidebar-border bg-transparent text-sidebar-foreground hover:bg-sidebar-accent"
              >
                <Link to="/register">Register as bidder</Link>
              </Button>
            </div>
          </div>
          <div className="rounded-xl border border-sidebar-border bg-sidebar-accent/40 p-6">
            <p className="text-xs font-semibold tracking-widest uppercase opacity-70">
              Live compliance snapshot
            </p>
            <div className="mt-4 space-y-3">
              {[
                ["PAN — Income Tax Department", "Verified"],
                ["GST — GSTN Public API", "Verified"],
                ["Udyam — MSME Portal", "Verified"],
                ["EPFO — Establishment Search", "Manual review"],
                ["Blacklist — Debarment Registry", "Clear"],
              ].map(([label, state]) => (
                <div
                  key={label}
                  className="flex items-center justify-between rounded-md bg-sidebar/70 px-3 py-2.5 text-sm"
                >
                  <span className="opacity-85">{label}</span>
                  <span
                    className={
                      state === "Manual review"
                        ? "font-semibold text-warning"
                        : "font-semibold text-success"
                    }
                  >
                    {state}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="border-b bg-background">
        <div className="mx-auto grid w-full max-w-6xl grid-cols-1 divide-y px-4 sm:grid-cols-3 sm:divide-x sm:divide-y-0">
          {[
            ["3", "Active tenders open for bidding"],
            ["1,284", "Verified bidder companies"],
            ["46 sec", "Average verification turnaround"],
          ].map(([value, label]) => (
            <div key={label} className="px-2 py-8 text-center">
              <p className="text-3xl font-extrabold text-primary">{value}</p>
              <p className="mt-1 text-sm text-muted-foreground">{label}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mx-auto w-full max-w-6xl px-4 py-20">
        <h2 className="text-2xl font-bold md:text-3xl">Why DIXY</h2>
        <p className="mt-2 max-w-2xl text-muted-foreground">
          Manual paperwork verification is where tenders lose weeks. DIXY removes that step without
          removing the audit trail.
        </p>
        <div className="mt-10 grid gap-5 md:grid-cols-2">
          {features.map((f) => (
            <Card key={f.title} className="border-border/70">
              <CardContent className="p-6">
                <span className="grid size-10 place-items-center rounded-md bg-accent/15 text-accent">
                  <f.icon className="size-5" />
                </span>
                <h3 className="mt-4 font-semibold">{f.title}</h3>
                <p className="mt-2 text-sm text-muted-foreground">{f.body}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      <section className="bg-surface">
        <div className="mx-auto w-full max-w-6xl px-4 py-20">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <h2 className="text-2xl font-bold md:text-3xl">Three steps to your first bid</h2>
              <p className="mt-2 text-muted-foreground">
                Verification happens once. Every tender after that reuses it.
              </p>
            </div>
            <Button asChild variant="outline">
              <Link to="/how-it-works">See the full process</Link>
            </Button>
          </div>
          <ol className="mt-10 grid gap-5 md:grid-cols-3">
            {steps.map((s, i) => (
              <li key={s.title} className="rounded-lg border bg-card p-6">
                <span className="text-xs font-bold tracking-widest text-accent">
                  STEP {i + 1}
                </span>
                <s.icon className="mt-3 size-6 text-primary" />
                <h3 className="mt-3 font-semibold">{s.title}</h3>
                <p className="mt-1 text-sm text-muted-foreground">{s.body}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>
    </PublicLayout>
  );
}
