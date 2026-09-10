import { Outlet, createFileRoute, useNavigate, useRouterState } from "@tanstack/react-router";
import { useEffect } from "react";

import { useAuth } from "@/lib/auth";
import { isOfficerRole } from "@/lib/types";

export const Route = createFileRoute("/_portal")({
  ssr: false,
  component: PortalLayout,
});

function PortalLayout() {
  const { user, bidder, ready } = useAuth();
  const navigate = useNavigate();
  const pathname = useRouterState({ select: (s) => s.location.pathname });

  const needsCompany =
    !!user && !isOfficerRole(user.role) && !bidder && pathname !== "/select-company";

  useEffect(() => {
    if (!ready) return;
    if (!user) navigate({ to: "/login", replace: true });
    else if (needsCompany) navigate({ to: "/select-company", replace: true });
  }, [ready, user, needsCompany, navigate]);

  if (!ready || !user || needsCompany) {
    return (
      <div className="grid min-h-screen place-items-center bg-surface">
        <div className="h-10 w-10 animate-spin rounded-full border-2 border-primary border-t-transparent" />
      </div>
    );
  }

  return <Outlet />;
}
