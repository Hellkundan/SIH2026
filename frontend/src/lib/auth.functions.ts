import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";
import { API_BASE_URL } from "./api/client";

import type { AuthUser, Role } from "./types";

const credentials = z.object({
  username: z.string().min(1),
  password: z.string().min(1),
});

/** Demo logins used while SPRING_API_BASE_URL is not configured. */
const DEMO_ACCOUNTS: Record<string, Role> = {
  bidder: "BIDDER",
  officer: "PROCUREMENT_OFFICER",
  admin: "ADMIN",
};

const normaliseRole = (value: unknown): Role => {
  const role = String(value ?? "BIDDER")
    .toUpperCase()
    .replace(/^ROLE_/, "");
  if (role === "ADMIN") return "ADMIN";
  if (role === "PROCUREMENT_OFFICER" || role === "OFFICER") return "PROCUREMENT_OFFICER";
  return "BIDDER";
};

/**
 * Proxies sign-in to the Spring Boot backend so the browser never hits CORS.
 * The backend accepts { username, password } and answers with the standard
 * envelope { status, data, message, timestamp } where data is
 * { token, username, role } — no id, email or bidderId.
 */
export const login = createServerFn({ method: "POST" })
  .inputValidator((data: unknown) => credentials.parse(data))
  .handler(async ({ data }): Promise<AuthUser> => {
    const base = API_BASE_URL;

    if (base) {
      const res = await fetch(`${base.replace(/\/$/, "")}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: data.username, password: data.password }),
      });
      if (!res.ok) throw new Error("Invalid username or password");
      const body = (await res.json()) as { data?: Record<string, unknown> };
      const payload = body?.data ?? (body as unknown as Record<string, unknown>);
      return {
        username: String(payload["username"] ?? data.username),
        role: normaliseRole(payload["role"]),
        token: String(payload["token"] ?? ""),
      };
    }

    const username = data.username.trim().toLowerCase();
    const role = DEMO_ACCOUNTS[username];
    if (!role || data.password !== "dixy1234") {
      throw new Error("Invalid username or password");
    }
    return { username, role, token: `demo.${username}.token` };
  });
