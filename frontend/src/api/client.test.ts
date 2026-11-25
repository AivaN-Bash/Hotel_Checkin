import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiTimeoutError, requestJson } from "./client";

afterEach(() => vi.unstubAllGlobals());

describe("requestJson", () => {
  it("normalizes server errors to the shared response shape", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            timestamp: "now",
            status: 503,
            code: "SERVICE_UNAVAILABLE",
            message: "Try later",
          }),
          { status: 503 },
        ),
      ),
    );
    await expect(requestJson("/v1/status")).rejects.toMatchObject({
      body: { status: 503, code: "SERVICE_UNAVAILABLE", message: "Try later" },
    });
  });

  it("aborts a request at its configured timeout", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(
        (_url: string, init?: RequestInit) =>
          new Promise((_resolve, reject) => {
            init?.signal?.addEventListener("abort", () =>
              reject(new DOMException("Aborted", "AbortError")),
            );
          }),
      ),
    );
    await expect(requestJson("/v1/status", {}, 5)).rejects.toBeInstanceOf(
      ApiTimeoutError,
    );
  });
});
