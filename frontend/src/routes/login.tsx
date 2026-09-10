import { Link, createFileRoute, useNavigate } from "@tanstack/react-router";
import { useServerFn } from "@tanstack/react-start";
import { useState } from "react";
import { toast } from "sonner";

import { Logo } from "@/components/Logo";
import { ModeBadge } from "@/components/ModeBadge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { login } from "@/lib/auth.functions";
import { useAuth } from "@/lib/auth";
import { isOfficerRole } from "@/lib/types";

export const Route = createFileRoute("/login")({
  head: () => ({
    meta: [
      { title: "Sign in — DIXY Procurement Platform" },
      {
        name: "description",
        content: "Sign in to DIXY to manage tenders, upload compliance documents and track bids.",
      },
      { property: "og:title", content: "Sign in — DIXY" },
      { property: "og:description", content: "Access your DIXY bidder or officer portal." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: LoginPage,
});

function LoginPage() {
  const doLogin = useServerFn(login);
  const { signIn, bidder } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const user = await doLogin({ data: { username, password } });
      signIn(user);
      toast.success(`Welcome back, ${user.username}`);
      if (isOfficerRole(user.role)) navigate({ to: "/officer", replace: true });
      else navigate({ to: bidder ? "/dashboard" : "/select-company", replace: true });
    } catch {
      setError("That username and password combination didn't work. Please try again.");
    } finally {
      setBusy(false);
    }
  }

  const fill = (who: "bidder" | "officer" | "admin") => {
    setUsername(who);
    setPassword("dixy1234");
    setError(null);
  };

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="hidden flex-col justify-between bg-sidebar p-12 text-sidebar-foreground lg:flex">
        <Logo invert />
        <div>
          <h2 className="text-3xl font-extrabold">One sign-in. Every tender you can bid on.</h2>
          <p className="mt-4 max-w-md text-sidebar-foreground/75">
            Attach your compliance documents to a bid and they are read and checked against the
            issuing registries automatically.
          </p>
        </div>
        <p className="text-xs text-sidebar-foreground/60">
          Demonstration environment — no live government data is exchanged.
        </p>
      </div>

      <div className="flex items-center justify-center bg-background px-4 py-12">
        <div className="w-full max-w-sm">
          <div className="flex items-center justify-between lg:justify-end">
            <div className="lg:hidden">
              <Logo />
            </div>
            <ModeBadge />
          </div>
          <h1 className="mt-8 text-2xl font-bold">Sign in</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Use the username issued to you — your role is detected automatically.
          </p>

          <form onSubmit={onSubmit} className="mt-8 space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                autoComplete="username"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. aarav.infratech"
              />
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Password</Label>
                <button
                  type="button"
                  className="text-xs text-accent hover:underline"
                  onClick={() =>
                    toast.info("Contact the procurement helpdesk to have your password reset.")
                  }
                >
                  Forgot password?
                </button>
              </div>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            {error ? (
              <p className="rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive">
                {error}
              </p>
            ) : null}

            <Button type="submit" className="w-full" disabled={busy}>
              {busy ? "Signing in…" : "Sign in"}
            </Button>
          </form>

          <div className="mt-6 rounded-md border bg-surface p-3 text-xs text-muted-foreground">
            <p className="font-semibold text-foreground">Demo accounts</p>
            <div className="mt-2 flex flex-wrap gap-2">
              <Button type="button" size="sm" variant="outline" onClick={() => fill("bidder")}>
                Bidder
              </Button>
              <Button type="button" size="sm" variant="outline" onClick={() => fill("officer")}>
                Procurement officer
              </Button>
              <Button type="button" size="sm" variant="outline" onClick={() => fill("admin")}>
                Admin
              </Button>
            </div>
          </div>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            New vendor?{" "}
            <Link to="/register" className="font-semibold text-accent hover:underline">
              Request an account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
