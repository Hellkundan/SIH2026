/**
 * Single swap point between demo data and the live Spring Boot API.
 *
 * Flip USE_MOCK to false (and set VITE_API_BASE_URL) to route every call in
 * `src/lib/api/index.ts` at the real backend. Page components never change.
 */
export const USE_MOCK =
  (import.meta.env["VITE_USE_MOCK"] ?? "true") !== "false";

export const API_BASE_URL =
  (import.meta.env["VITE_API_BASE_URL"] as string | undefined) ?? "";

const TOKEN_KEY = "dixy.token";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (typeof window === "undefined") return;
  if (token) window.localStorage.setItem(TOKEN_KEY, token);
  else window.localStorage.removeItem(TOKEN_KEY);
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
    this.name = "ApiError";
  }
}

/** Every backend response is wrapped as { status, data, message, timestamp }. */
interface Envelope<T> {
  status?: string | number;
  data?: T;
  message?: string;
  timestamp?: string;
}

/** Unwraps the envelope; falls back to the raw body if it is not wrapped. */
export function unwrap<T>(body: unknown): T {
  if (body && typeof body === "object" && "data" in (body as Envelope<T>)) {
    return (body as Envelope<T>).data as T;
  }
  return body as T;
}

export async function http<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken();
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init.headers ?? {}),
    },
  });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = (await res.json()) as Envelope<unknown>;
      if (body?.message) message = body.message;
    } catch {
      /* non-JSON error body */
    }
    throw new ApiError(res.status, message);
  }
  if (res.status === 204) return undefined as T;
  return unwrap<T>(await res.json());
}

/** Small latency so loading states are real in the demo build. */
export const delay = (ms = 320) => new Promise((r) => setTimeout(r, ms));
