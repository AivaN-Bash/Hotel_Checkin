export type KioskStep =
  | "welcome"
  | "lookup"
  | "confirm"
  | "verification"
  | "deposit"
  | "assigned"
  | "success";
export type LookupMethod = "code" | "guest";
export type KioskMode = "checkin" | "checkout";

export interface BookingDetails {
  id: string;
  guestName: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
}

export interface CheckinDetails {
  id: string;
  guestName: string;
  roomType: string;
  roomNumber: string;
  floor: number;
  digitalKey: string;
  createdAt: string;
}

export interface KioskState {
  step: KioskStep;
  mode: KioskMode;
  language: "en" | "th";
  theme: "light" | "dark";
  lookupMethod: LookupMethod;
  bookingCode: string;
  lastName: string;
  phoneNumber: string;
  booking: BookingDetails | null;
  checkin: CheckinDetails | null;
  error: { code: string; message: string } | null;
  busy: boolean;
  idempotencyKey: string;
}

export type KioskAction =
  | { type: "SET_LANGUAGE"; language: "en" | "th" }
  | { type: "SET_MODE"; mode: KioskMode }
  | { type: "TOGGLE_THEME" }
  | { type: "SET_LOOKUP_METHOD"; method: LookupMethod }
  | { type: "SET_BOOKING_CODE"; value: string }
  | { type: "SET_LAST_NAME"; value: string }
  | { type: "SET_PHONE"; value: string }
  | { type: "LOOKUP_STARTED" }
  | { type: "LOOKUP_SUCCEEDED"; booking: BookingDetails }
  | { type: "CHECKIN_STARTED" }
  | { type: "CHECKIN_SUCCEEDED"; checkin: CheckinDetails }
  | { type: "CHECKOUT_STARTED" }
  | { type: "CHECKOUT_SUCCEEDED" }
  | { type: "REQUEST_FAILED"; code: string; message: string }
  | { type: "CLEAR_ERROR" }
  | { type: "NEXT" }
  | { type: "BACK" }
  | { type: "START_OVER" };
