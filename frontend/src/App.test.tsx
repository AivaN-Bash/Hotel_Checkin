import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "./App";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  document.documentElement.classList.remove("dark");
  delete document.documentElement.dataset.theme;
});

describe("kiosk availability screens", () => {
  it("applies the selected theme to HeroUI controls", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: "OPERATIONAL",
            components: { api: "UP", database: "UP" },
            message: "Ready",
            estimatedBackAt: null,
            checkedAt: "2026-11-25T00:00:00Z",
          }),
          { status: 200, headers: { "Content-Type": "application/json" } },
        ),
      ),
    );
    const user = userEvent.setup();
    render(<App />);

    const themeButton = await screen.findByRole("button", {
      name: "Switch color theme",
    });
    expect(document.documentElement).not.toHaveClass("dark");

    await user.click(themeButton);
    await waitFor(() => expect(document.documentElement).toHaveClass("dark"));
    expect(document.documentElement.dataset.theme).toBe("hotel-dark");
  });

  it("shows backend maintenance details and returns to welcome after recovery", async () => {
    const response = (status: string, message: string) =>
      new Response(
        JSON.stringify({
          status,
          components: { api: "UP", database: "UP" },
          message,
          estimatedBackAt: null,
          checkedAt: "2026-11-25T00:00:00Z",
        }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      );
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        response("MAINTENANCE", "We are making a small improvement."),
      )
      .mockResolvedValueOnce(response("OPERATIONAL", "Ready"));
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    render(<App />);
    expect(
      await screen.findByRole("heading", { name: "We’ll be back shortly." }),
    ).toBeInTheDocument();
    expect(
      screen.getByText("We are making a small improvement."),
    ).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Try again" }));
    await waitFor(() =>
      expect(
        screen.getByRole("button", { name: "Begin check-in" }),
      ).toBeInTheDocument(),
    );
  });
});
