import { Link, createFileRoute } from "@tanstack/react-router";

import { PublicLayout } from "@/components/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export const Route = createFileRoute("/how-it-works")({
  head: () => ({
    meta: [
      { title: "How DIXY Works — From Registration to Award" },
      {
        name: "description",
        content:
          "See how bidders get verified and how procurement officers publish tenders, review automated compliance results and award contracts on DIXY.",
      },
      { property: "og:title", content: "How DIXY Works" },
      {
        property: "og:description",
        content: "The bidder journey, the officer journey and the verification pipeline explained.",
      },
    ],
  }),
  component: HowItWorks,
});

const bidderSteps = [
  ["Create your account", "Register your company with its name, email and phone number."],
  [
    "Upload compliance documents",
    "Drag in PAN, GST, Udyam, EPFO, ESIC and any certificates you hold.",
  ],
  [
    "Automatic verification",
    "OCR extracts the fields, then each one is checked against its government registry.",
  ],
  [
    "Browse matching tenders",
    "Open tenders show whether your verified profile already meets their requirements.",
  ],
  ["Submit your bid", "Attach already-verified documents, add your quote and submit."],
  [
    "Track the outcome",
    "Draft → Submitted → Under review → Qualified or Disqualified, with officer remarks.",
  ],
];

const officerSteps = [
  ["Publish a tender", "Create it as a draft, then open it for bidding when you're ready."],
  [
    "Define requirements",
    "Tick the mandatory certificates and add criteria like minimum turnover.",
  ],
  [
    "Review verified bids",
    "Each bid arrives with a compliance score and every registry response attached.",
  ],
  [
    "Qualify or disqualify",
    "Record remarks against the decision; the bidder sees the outcome immediately.",
  ],
  ["Award", "Shortlist from a pool where the paperwork has already been proven genuine."],
];

const pipeline = [
  ["Document", "PDF or image uploaded by the bidder"],
  ["OCR extraction", "Numbers, names and dates pulled from the scan"],
  ["Confidence score", "Blurry or low-confidence scans get flagged"],
  ["Registry check", "Verification Hub queries the issuing authority"],
  ["Result", "Verified, Not found, Failed, Pending or Manual review"],
];

function HowItWorks() {
  return (
    <PublicLayout>
      <section className="border-b bg-sidebar text-sidebar-foreground">
        <div className="mx-auto w-full max-w-6xl px-4 py-16">
          <h1 className="text-3xl font-extrabold md:text-4xl">How DIXY works</h1>
          <p className="mt-3 max-w-2xl text-sidebar-foreground/75">
            One verification pipeline serves both sides of a tender: bidders prove their compliance
            once, officers review evidence instead of chasing it.
          </p>
        </div>
      </section>

      <section className="mx-auto w-full max-w-6xl px-4 py-16">
        <h2 className="text-2xl font-bold">For bidders</h2>
        <ol className="mt-8 space-y-4">
          {bidderSteps.map(([title, body], i) => (
            <li key={title} className="flex gap-4 rounded-lg border bg-card p-5">
              <span className="grid size-8 shrink-0 place-items-center rounded-full bg-primary text-sm font-bold text-primary-foreground">
                {i + 1}
              </span>
              <div>
                <h3 className="font-semibold">{title}</h3>
                <p className="mt-1 text-sm text-muted-foreground">{body}</p>
              </div>
            </li>
          ))}
        </ol>
      </section>

      <section className="bg-surface">
        <div className="mx-auto w-full max-w-6xl px-4 py-16">
          <h2 className="text-2xl font-bold">The verification pipeline</h2>
          <div className="mt-8 grid gap-3 md:grid-cols-5">
            {pipeline.map((step, i) => (
              <Card key={step[0]} className="relative border-border/70">
                <CardContent className="p-5">
                  <span className="text-xs font-bold tracking-widest text-accent">0{i + 1}</span>
                  <h3 className="mt-2 text-sm font-semibold">{step[0]}</h3>
                  <p className="mt-1 text-xs text-muted-foreground">{step[1]}</p>
                </CardContent>
              </Card>
            ))}
          </div>
          <pre className="mt-8 overflow-x-auto rounded-lg border bg-card p-5 text-xs leading-relaxed text-muted-foreground">
            {`  [Upload] --> [OCR extraction] --> [Confidence score]
                                          |
                              low  <------+------>  good
                                |                     |
                       [Manual review]        [Registry check]
                                                      |
                                    Verified / Not found / Failed`}
          </pre>
        </div>
      </section>

      <section className="mx-auto w-full max-w-6xl px-4 py-16">
        <h2 className="text-2xl font-bold">For procurement officers</h2>
        <ol className="mt-8 grid gap-4 md:grid-cols-2">
          {officerSteps.map(([title, body], i) => (
            <li key={title} className="rounded-lg border bg-card p-5">
              <span className="text-xs font-bold tracking-widest text-accent">STEP {i + 1}</span>
              <h3 className="mt-2 font-semibold">{title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{body}</p>
            </li>
          ))}
        </ol>
        <div className="mt-10 flex flex-wrap gap-3">
          <Button asChild size="lg">
            <Link to="/register">Register as a bidder</Link>
          </Button>
          <Button asChild size="lg" variant="outline">
            <Link to="/login">Officer login</Link>
          </Button>
        </div>
      </section>
    </PublicLayout>
  );
}
