import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "./App";

afterEach(() => vi.unstubAllGlobals());

describe("self check-in journey", () => {
  it("looks up a booking, completes the mock steps, and shows the digital key", async () => {
    const booking = {
      id: "10000000-0000-0000-0000-000000000001",
      guestName: "Alex Rivera",
      roomType: "Deluxe King",
      checkInDate: "2026-11-25",
      checkOutDate: "2026-11-27",
      guestCount: 2,
    };
    const checkin = {
      id: "30000000-0000-0000-0000-000000000001",
      guestName: "Alex Rivera",
      roomType: "Deluxe King",
      roomNumber: "201",
      floor: 2,
      digitalKey: "DEMO-KEY-30000000",
      createdAt: "2026-11-25T00:00:00Z",
    };
    const json = (body: unknown) =>
      new Response(JSON.stringify(body), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        json({
          status: "OPERATIONAL",
          components: { api: "UP", database: "UP" },
          message: "Ready",
          estimatedBackAt: null,
          checkedAt: "2026-11-25T00:00:00Z",
        }),
      )
      .mockResolvedValueOnce(json(booking))
      .mockResolvedValueOnce(json(checkin));
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    render(<App />);

    await user.click(
      await screen.findByRole("button", { name: "Begin check-in" }),
    );
    await user.type(screen.getByLabelText("Booking code"), "DEMO2026");
    await user.click(screen.getByRole("button", { name: "Find my booking" }));
    expect(
      await screen.findByRole("heading", { name: "Please review your stay" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Alex Rivera")).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", { name: "Details look right" }),
    );
    await user.click(screen.getByRole("button", { name: "Confirm identity" }));
    await user.click(
      screen.getByRole("button", { name: "Confirm demo deposit" }),
    );
    expect(
      await screen.findByRole("heading", { name: "Your room is ready" }),
    ).toBeInTheDocument();
    expect(screen.getByText("201")).toBeInTheDocument();
    expect(screen.getByLabelText("DIGITAL KEY · DEMO")).toBeInTheDocument();

    const checkinRequest = fetchMock.mock.calls.find(([url]) =>
      String(url).endsWith("/v1/checkins"),
    );
    expect(checkinRequest?.[1]?.headers).toMatchObject({
      "Idempotency-Key": expect.any(String),
    });
    expect(checkinRequest?.[1]?.body).toBe(
      JSON.stringify({ bookingId: booking.id }),
    );
    await user.click(screen.getByRole("button", { name: "Continue" }));
    await waitFor(() =>
      expect(
        screen.getByRole("heading", { name: "You’re all checked in" }),
      ).toBeInTheDocument(),
    );
  });
});
