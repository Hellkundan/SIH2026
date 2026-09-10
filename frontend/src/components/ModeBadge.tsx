import { Database, Wifi } from "lucide-react";

import { API_BASE_URL, USE_MOCK } from "@/lib/api/client";
import { cn } from "@/lib/utils";

/**
 * Tells you at a glance whether the screens are reading demo data or the live
 * Spring Boot API. Driven by VITE_USE_MOCK / VITE_API_BASE_URL.
 */
export function ModeBadge({ className }: { className?: string }) {
  const live = !USE_MOCK;
  return (
    <span
      title={live ? `Connected to ${API_BASE_URL || "the configured API"}` : "Demo data — no API configured"}
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-semibold",
        live
          ? "border-success/40 bg-success/12 text-success"
          : "border-warning/40 bg-warning/15 text-warning-foreground",
        className,
      )}
    >
      {live ? <Wifi className="size-3" /> : <Database className="size-3" />}
      {live ? "Live API" : "Demo data"}
    </span>
  );
}

/** Fixed corner variant for portal screens. */
export function ModeBadgeCorner() {
  return (
    <div className="pointer-events-none fixed bottom-4 left-4 z-50">
      <ModeBadge className="pointer-events-auto shadow-sm backdrop-blur" />
    </div>
  );
}
