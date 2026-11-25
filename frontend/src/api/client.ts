import type { ApiErrorBody } from "../types/service";

const DEFAULT_TIMEOUT_MS = 5_000;
const API_BASE_URL = (import.meta.env.VITE_API_URL || "/api").replace(
  /\/$/,
  "",
);

export class ApiError extends Error {
  constructor(public readonly body: ApiErrorBody) {
    super(body.message);
    this.name = "ApiError";
  }
}

export class ApiTimeoutError extends Error {
  constructor() {
    super("The server did not respond in time.");
    this.name = "ApiTimeoutError";
  }
}

export async function requestJson<T>(
  path: string,
  init: RequestInit = {},
  timeoutMs = DEFAULT_TIMEOUT_MS,
): Promise<T> {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);
  try {
    const response = await fetch(
      `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`,
      {
        ...init,
        signal: init.signal
          ? AbortSignal.any([controller.signal, init.signal])
          : controller.signal,
        headers: { Accept: "application/json", ...init.headers },
      },
    );
    const payload: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      throw new ApiError(normalizeError(payload, response.status));
    }
    return payload as T;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    if (error instanceof DOMException && error.name === "AbortError")
      throw new ApiTimeoutError();
    throw error;
  } finally {
    window.clearTimeout(timeoutId);
  }
}

function normalizeError(payload: unknown, status: number): ApiErrorBody {
  if (isApiErrorBody(payload)) return payload;
  return {
    timestamp: new Date().toISOString(),
    status,
    code: "REQUEST_FAILED",
    message: "The request could not be completed. Please try again.",
  };
}

function isApiErrorBody(value: unknown): value is ApiErrorBody {
  return (
    typeof value === "object" &&
    value !== null &&
    "status" in value &&
    "code" in value &&
    "message" in value &&
    "timestamp" in value
  );
}
