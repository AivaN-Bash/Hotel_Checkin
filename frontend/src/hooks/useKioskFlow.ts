import { useReducer } from "react";
import type { KioskAction, KioskState } from "../types/kiosk";

function newIdempotencyKey(): string {
  return crypto.randomUUID();
}

export function initialKioskState(): KioskState {
  return {
    step: "welcome",
    mode: "checkin",
    language: "en",
    theme: "light",
    lookupMethod: "code",
    bookingCode: "",
    lastName: "",
    phoneNumber: "",
    booking: null,
    checkin: null,
    error: null,
    busy: false,
    idempotencyKey: newIdempotencyKey(),
  };
}

export function kioskReducer(
  state: KioskState,
  action: KioskAction,
): KioskState {
  switch (action.type) {
    case "SET_LANGUAGE":
      return { ...state, language: action.language };
    case "SET_MODE":
      return { ...state, mode: action.mode, step: "lookup", error: null };
    case "TOGGLE_THEME":
      return { ...state, theme: state.theme === "light" ? "dark" : "light" };
    case "SET_LOOKUP_METHOD":
      return { ...state, lookupMethod: action.method, error: null };
    case "SET_BOOKING_CODE":
      return { ...state, bookingCode: action.value, error: null };
    case "SET_LAST_NAME":
      return { ...state, lastName: action.value, error: null };
    case "SET_PHONE":
      return { ...state, phoneNumber: action.value, error: null };
    case "LOOKUP_STARTED":
      return { ...state, busy: true, error: null };
    case "LOOKUP_SUCCEEDED":
      return {
        ...state,
        busy: false,
        booking: action.booking,
        error: null,
        step: "confirm",
      };
    case "CHECKIN_STARTED":
      return { ...state, busy: true, error: null };
    case "CHECKIN_SUCCEEDED":
      return {
        ...state,
        busy: false,
        checkin: action.checkin,
        error: null,
        step: "assigned",
      };
    case "CHECKOUT_SUCCEEDED":
      return { ...state, busy: false, error: null, step: "success" };
    case "REQUEST_FAILED":
      return {
        ...state,
        busy: false,
        error: { code: action.code, message: action.message },
      };
    case "CLEAR_ERROR":
      return { ...state, error: null };
    case "NEXT":
      return { ...state, error: null, step: nextStep(state.step) };
    case "BACK":
      return { ...state, error: null, step: previousStep(state.step) };
    case "START_OVER":
      return initialKioskStateWithPreferences(state);
  }
}

function nextStep(step: KioskState["step"]): KioskState["step"] {
  switch (step) {
    case "welcome":
      return "lookup";
    case "lookup":
      return "confirm";
    case "confirm":
      return "verification";
    case "verification":
      return "deposit";
    case "deposit":
      return "assigned";
    case "assigned":
      return "success";
    case "success":
      return "welcome";
  }
}

function previousStep(step: KioskState["step"]): KioskState["step"] {
  switch (step) {
    case "welcome":
      return "welcome";
    case "lookup":
      return "welcome";
    case "confirm":
      return "lookup";
    case "verification":
      return "confirm";
    case "deposit":
      return "verification";
    case "assigned":
      return "deposit";
    case "success":
      return "welcome";
  }
}

function initialKioskStateWithPreferences(state: KioskState): KioskState {
  return {
    ...initialKioskState(),
    language: state.language,
    theme: state.theme,
  };
}

export function useKioskFlow() {
  return useReducer(kioskReducer, undefined, initialKioskState);
}
