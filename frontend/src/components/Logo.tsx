import { Link } from "@tanstack/react-router";
import { ShieldCheck } from "lucide-react";

import { cn } from "@/lib/utils";

export function Logo({ className, invert }: { className?: string; invert?: boolean }) {
  return (
    <Link to="/" className={cn("inline-flex items-center gap-2", className)}>
      <span
        className={cn(
          "grid size-8 place-items-center rounded-md",
          invert ? "bg-accent text-accent-foreground" : "bg-primary text-primary-foreground",
        )}
      >
        <ShieldCheck className="size-4.5" />
      </span>
      <span className="flex flex-col leading-none">
        <span
          className={cn(
            "text-lg font-extrabold tracking-tight",
            invert ? "text-sidebar-foreground" : "text-primary",
          )}
        >
          DIXY
        </span>
        <span className="text-[10px] font-medium tracking-widest text-muted-foreground uppercase">
          e-Tendering
        </span>
      </span>
    </Link>
  );
}
