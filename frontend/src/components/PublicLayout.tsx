import { Link } from "@tanstack/react-router";
import type { ReactNode } from "react";

import { Logo } from "@/components/Logo";
import { ModeBadge } from "@/components/ModeBadge";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/auth";
import { isOfficerRole } from "@/lib/types";

export function PublicLayout({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  return (
    <div className="flex min-h-screen flex-col bg-background">
      <header className="sticky top-0 z-40 border-b bg-background/90 backdrop-blur">
        <div className="mx-auto flex h-16 w-full max-w-6xl items-center justify-between px-4">
          <Logo />
          <nav className="hidden items-center gap-7 text-sm font-medium md:flex">
            <Link to="/" className="text-muted-foreground hover:text-foreground">
              Home
            </Link>
            <Link to="/how-it-works" className="text-muted-foreground hover:text-foreground">
              How it works
            </Link>
            <Link to="/tenders" className="text-muted-foreground hover:text-foreground">
              Tenders
            </Link>
          </nav>
          <div className="flex items-center gap-2">
            {user ? (
              <Button asChild size="sm">
                <Link to={isOfficerRole(user.role) ? "/officer" : "/dashboard"}>
                  Go to dashboard
                </Link>
              </Button>
            ) : (
              <>
                <Button asChild variant="ghost" size="sm">
                  <Link to="/login">Login</Link>
                </Button>
                <Button asChild size="sm">
                  <Link to="/register">Register</Link>
                </Button>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="flex-1">{children}</main>
      <footer className="border-t bg-sidebar text-sidebar-foreground">
        <div className="mx-auto grid w-full max-w-6xl gap-8 px-4 py-12 md:grid-cols-4">
          <div className="md:col-span-2">
            <Logo invert />
            <p className="mt-3 max-w-sm text-sm text-sidebar-foreground/70">
              DIXY brings automated document reading and live government registry checks to public
              procurement, so genuine vendors get through faster.
            </p>
          </div>
          <div>
            <h4 className="text-sm font-semibold">Platform</h4>
            <ul className="mt-3 space-y-2 text-sm text-sidebar-foreground/70">
              <li>
                <Link to="/how-it-works" className="hover:text-sidebar-foreground">
                  How it works
                </Link>
              </li>
              <li>
                <Link to="/tenders" className="hover:text-sidebar-foreground">
                  Browse tenders
                </Link>
              </li>
              <li>
                <Link to="/login" className="hover:text-sidebar-foreground">
                  Login
                </Link>
              </li>
              <li>
                <Link to="/register" className="hover:text-sidebar-foreground">
                  Register as bidder
                </Link>
              </li>
            </ul>
          </div>
          <div>
            <h4 className="text-sm font-semibold">Contact</h4>
            <ul className="mt-3 space-y-2 text-sm text-sidebar-foreground/70">
              <li>Procurement Helpdesk</li>
              <li>support@dixy.gov.in</li>
              <li>1800 000 0000 (10:00–18:00 IST)</li>
            </ul>
          </div>
        </div>
        <div className="flex flex-wrap items-center justify-center gap-3 border-t border-sidebar-border py-4 text-center text-xs text-sidebar-foreground/60">
          <span>© {new Date().getFullYear()} DIXY Procurement Platform.</span>
          <ModeBadge />
        </div>
      </footer>
    </div>
  );
}
