import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";

import { setToken } from "./api/client";
import type { AuthUser, BidderProfile } from "./types";

const USER_KEY = "dixy.user";
const BIDDER_KEY = "dixy.bidder";

interface AuthContextValue {
  user: AuthUser | null;
  /** Chosen by the bidder after sign-in, because login returns no bidderId. */
  bidder: BidderProfile | null;
  ready: boolean;
  signIn: (user: AuthUser) => void;
  signOut: () => void;
  setBidder: (profile: BidderProfile | null) => void;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  bidder: null,
  ready: false,
  signIn: () => {},
  signOut: () => {},
  setBidder: () => {},
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [bidder, setBidderState] = useState<BidderProfile | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    try {
      const raw = window.localStorage.getItem(USER_KEY);
      if (raw) {
        const parsed = JSON.parse(raw) as AuthUser;
        setUser(parsed);
        setToken(parsed.token);
      }
      const rawBidder = window.localStorage.getItem(BIDDER_KEY);
      if (rawBidder) setBidderState(JSON.parse(rawBidder) as BidderProfile);
    } catch {
      /* ignore corrupt storage */
    }
    setReady(true);
  }, []);

  const signIn = useCallback((next: AuthUser) => {
    window.localStorage.setItem(USER_KEY, JSON.stringify(next));
    setToken(next.token);
    setUser(next);
  }, []);

  const signOut = useCallback(() => {
    window.localStorage.removeItem(USER_KEY);
    window.localStorage.removeItem(BIDDER_KEY);
    setToken(null);
    setUser(null);
    setBidderState(null);
  }, []);

  const setBidder = useCallback((profile: BidderProfile | null) => {
    if (profile) window.localStorage.setItem(BIDDER_KEY, JSON.stringify(profile));
    else window.localStorage.removeItem(BIDDER_KEY);
    setBidderState(profile);
  }, []);

  const value = useMemo(
    () => ({ user, bidder, ready, signIn, signOut, setBidder }),
    [user, bidder, ready, signIn, signOut, setBidder],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);

/** Convenience for bidder-scoped screens. */
export function useBidderId(): string | null {
  const { bidder } = useAuth();
  return bidder?.id ?? null;
}
