import { requestJson } from "./client";
import type {
  BookingDetails,
  CheckinDetails,
  KioskState,
} from "../types/kiosk";

// Render's free instance can need close to a minute to wake after inactivity.
const BOOKING_LOOKUP_TIMEOUT_MS = 75_000;

export async function lookupBooking(
  state: KioskState,
): Promise<BookingDetails> {
  const body =
    state.lookupMethod === "code"
      ? { bookingCode: state.bookingCode.trim() }
      : {
          lastName: state.lastName.trim(),
          phoneNumber: state.phoneNumber.trim(),
        };
  return requestJson<BookingDetails>(
    "/v1/bookings/lookup",
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    },
    BOOKING_LOOKUP_TIMEOUT_MS,
  );
}

export async function completeCheckin(
  bookingId: string,
  idempotencyKey: string,
): Promise<CheckinDetails> {
  return requestJson<CheckinDetails>("/v1/checkins", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": idempotencyKey,
    },
    body: JSON.stringify({ bookingId }),
  });
}
