import { Link, createFileRoute } from "@tanstack/react-router";

import { Button } from "@/components/ui/button";

export const Route = createFileRoute("/$")({
  head: () => ({
    meta: [
      { title: "Page not found — DIXY" },
      { name: "description", content: "The DIXY page you were looking for does not exist." },
      { name: "robots", content: "noindex" },
      { property: "og:title", content: "Page not found — DIXY" },
      { property: "og:description", content: "This DIXY page does not exist." },
    ],
  }),
  component: NotFoundPage,
});

function NotFoundPage() {
  return (
    <div className="grid min-h-screen place-items-center bg-surface px-4">
      <div className="max-w-md text-center">
        <p className="text-6xl font-extrabold text-primary">404</p>
        <h1 className="mt-4 text-xl font-semibold">We couldn't find that page</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          The link may be out of date, or the tender you were looking for has been withdrawn.
        </p>
        <div className="mt-6 flex justify-center gap-3">
          <Button asChild>
            <Link to="/">Back to home</Link>
          </Button>
          <Button asChild variant="outline">
            <Link to="/dashboard">Go to dashboard</Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
