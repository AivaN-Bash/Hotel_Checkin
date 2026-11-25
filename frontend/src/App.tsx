import { useCallback, useEffect, useRef } from "react";
import { ApiError, ApiTimeoutError } from "./api/client";
import {
  completeCheckin,
  completeCheckout,
  lookupBooking,
} from "./api/bookings";
import { IdleWarning } from "./components/IdleWarning";
import {
  MaintenanceScreen,
  WakingScreen,
} from "./components/MaintenanceScreen";
import { copy } from "./i18n/kiosk";
import { useIdleTimer } from "./hooks/useIdleTimer";
import { useKioskFlow } from "./hooks/useKioskFlow";
import { useServiceStatus } from "./hooks/useServiceStatus";
import {
  AssignedRoomScreen,
  BookingLookupScreen,
  ConfirmBookingScreen,
  DepositScreen,
  SuccessScreen,
  VerificationScreen,
  WelcomeScreen,
} from "./pages/FlowScreens";
import { ComponentPreviewPage } from "./pages/ComponentPreviewPage";

const MAINTENANCE_RESUME_WINDOW_MS = 5 * 60_000;
const SUCCESS_RESET_MS = 12_000;

export default function App() {
  if (window.location.pathname === "/dev/components")
    return <ComponentPreviewPage />;
  return <KioskApp />;
}

function KioskApp() {
  const [flow, dispatch] = useKioskFlow();
  const service = useServiceStatus();
  const maintenanceStartedAt = useRef<number | null>(null);
  const maintenanceResetTimer = useRef<number | null>(null);
  const requestInProgress = useRef(false);
  const t = copy[flow.language];

  useEffect(() => {
    document.documentElement.dataset.theme = `hotel-${flow.theme}`;
    document.documentElement.lang = flow.language;
  }, [flow.theme, flow.language]);

  useEffect(() => {
    const paused =
      service.view === "maintenance" || service.view === "unavailable";
    if (paused && maintenanceStartedAt.current === null) {
      maintenanceStartedAt.current = Date.now();
      maintenanceResetTimer.current = window.setTimeout(
        () => dispatch({ type: "START_OVER" }),
        MAINTENANCE_RESUME_WINDOW_MS,
      );
    }
    if (
      service.view === "operational" &&
      maintenanceStartedAt.current !== null
    ) {
      const elapsed = Date.now() - maintenanceStartedAt.current;
      if (elapsed >= MAINTENANCE_RESUME_WINDOW_MS)
        dispatch({ type: "START_OVER" });
      if (maintenanceResetTimer.current !== null)
        window.clearTimeout(maintenanceResetTimer.current);
      maintenanceStartedAt.current = null;
      maintenanceResetTimer.current = null;
    }
  }, [service.view]);

  useEffect(() => {
    if (flow.step !== "success") return;
    const timeout = window.setTimeout(
      () => dispatch({ type: "START_OVER" }),
      SUCCESS_RESET_MS,
    );
    return () => window.clearTimeout(timeout);
  }, [flow.step]);

  const resetFlow = useCallback(() => dispatch({ type: "START_OVER" }), []);
  const idle = useIdleTimer(
    service.view === "operational" && flow.step !== "success",
    resetFlow,
  );

  const startOver = () => dispatch({ type: "START_OVER" });
  const goBack = () => dispatch({ type: "BACK" });
  const changeLanguage = () =>
    dispatch({
      type: "SET_LANGUAGE",
      language: flow.language === "en" ? "th" : "en",
    });
  const toggleTheme = () => dispatch({ type: "TOGGLE_THEME" });

  const findBooking = async () => {
    if (requestInProgress.current) return;
    if (
      flow.lookupMethod === "code"
        ? !flow.bookingCode.trim()
        : !flow.lastName.trim() || !flow.phoneNumber.trim()
    ) {
      dispatch({
        type: "REQUEST_FAILED",
        code: "VALIDATION_ERROR",
        message: t.errorValidation,
      });
      return;
    }
    requestInProgress.current = true;
    dispatch({ type: "LOOKUP_STARTED" });
    try {
      const booking = await lookupBooking(flow);
      dispatch({ type: "LOOKUP_SUCCEEDED", booking });
    } catch (error) {
      dispatch({ type: "REQUEST_FAILED", ...readError(error, flow.language) });
    } finally {
      requestInProgress.current = false;
    }
  };

  const finishPayment = async () => {
    if (!flow.booking || requestInProgress.current) return;
    requestInProgress.current = true;
    dispatch({ type: "CHECKIN_STARTED" });
    try {
      const checkin = await completeCheckin(
        flow.booking.id,
        flow.idempotencyKey,
      );
      dispatch({ type: "CHECKIN_SUCCEEDED", checkin });
    } catch (error) {
      dispatch({ type: "REQUEST_FAILED", ...readError(error, flow.language) });
    } finally {
      requestInProgress.current = false;
    }
  };

  const finishCheckout = async () => {
    if (!flow.booking || requestInProgress.current) return;
    requestInProgress.current = true;
    dispatch({ type: "CHECKOUT_STARTED" });
    try {
      await completeCheckout(flow.booking.id);
      dispatch({ type: "CHECKOUT_SUCCEEDED" });
    } catch (error) {
      dispatch({ type: "REQUEST_FAILED", ...readError(error, flow.language) });
    } finally {
      requestInProgress.current = false;
    }
  };

  if (service.view === "warming")
    return (
      <WakingScreen
        language={flow.language}
        onLanguageChange={changeLanguage}
      />
    );
  if (service.view === "maintenance" || service.view === "unavailable") {
    return (
      <MaintenanceScreen
        language={flow.language}
        message={service.status?.message}
        estimatedBackAt={service.status?.estimatedBackAt}
        lastCheckedAt={service.lastCheckedAt}
        onRetry={service.retry}
        onLanguageChange={changeLanguage}
      />
    );
  }

  let screen;
  switch (flow.step) {
    case "welcome":
      screen = (
        <WelcomeScreen
          language={flow.language}
          theme={flow.theme}
          onLanguageChange={(language) =>
            dispatch({ type: "SET_LANGUAGE", language })
          }
          onThemeToggle={toggleTheme}
          onStart={() => dispatch({ type: "NEXT" })}
          onStartCheckout={() =>
            dispatch({ type: "SET_MODE", mode: "checkout" })
          }
        />
      );
      break;
    case "lookup":
      screen = (
        <BookingLookupScreen
          language={flow.language}
          theme={flow.theme}
          method={flow.lookupMethod}
          bookingCode={flow.bookingCode}
          lastName={flow.lastName}
          phoneNumber={flow.phoneNumber}
          error={flow.error}
          busy={flow.busy}
          onLanguageChange={changeLanguage}
          onThemeToggle={toggleTheme}
          onBack={goBack}
          onCancel={startOver}
          onMethodChange={(method) =>
            dispatch({ type: "SET_LOOKUP_METHOD", method })
          }
          onBookingCodeChange={(value) =>
            dispatch({ type: "SET_BOOKING_CODE", value })
          }
          onLastNameChange={(value) =>
            dispatch({ type: "SET_LAST_NAME", value })
          }
          onPhoneChange={(value) => dispatch({ type: "SET_PHONE", value })}
          onSubmit={() => void findBooking()}
        />
      );
      break;
    case "confirm":
      screen = flow.booking && (
        <ConfirmBookingScreen
          language={flow.language}
          theme={flow.theme}
          booking={flow.booking}
          checkout={flow.mode === "checkout"}
          error={flow.error}
          busy={flow.busy}
          onLanguageChange={changeLanguage}
          onThemeToggle={toggleTheme}
          onBack={goBack}
          onCancel={startOver}
          onContinue={() =>
            flow.mode === "checkout"
              ? void finishCheckout()
              : dispatch({ type: "NEXT" })
          }
        />
      );
      break;
    case "verification":
      screen = (
        <VerificationScreen
          language={flow.language}
          theme={flow.theme}
          onLanguageChange={changeLanguage}
          onThemeToggle={toggleTheme}
          onBack={goBack}
          onCancel={startOver}
          onContinue={() => dispatch({ type: "NEXT" })}
        />
      );
      break;
    case "deposit":
      screen = (
        <DepositScreen
          language={flow.language}
          theme={flow.theme}
          error={flow.error?.message ?? null}
          busy={flow.busy}
          onLanguageChange={changeLanguage}
          onThemeToggle={toggleTheme}
          onBack={goBack}
          onCancel={startOver}
          onComplete={() => void finishPayment()}
        />
      );
      break;
    case "assigned":
      screen = flow.checkin && (
        <AssignedRoomScreen
          language={flow.language}
          theme={flow.theme}
          checkin={flow.checkin}
          onLanguageChange={changeLanguage}
          onThemeToggle={toggleTheme}
          onCancel={startOver}
          onContinue={() => dispatch({ type: "NEXT" })}
        />
      );
      break;
    case "success":
      screen = (
        <SuccessScreen
          language={flow.language}
          checkedOut={flow.mode === "checkout"}
          onFinish={startOver}
        />
      );
      break;
  }

  return (
    <>
      {screen}
      <IdleWarning
        open={idle.isWarning}
        secondsRemaining={idle.secondsRemaining}
        language={flow.language}
        onStay={() => window.dispatchEvent(new Event("pointerdown"))}
        onReset={startOver}
      />
    </>
  );
}

function readError(
  error: unknown,
  language: "en" | "th",
): { code: string; message: string } {
  const t = copy[language];
  if (error instanceof ApiError) {
    if (error.body.code === "BOOKING_NOT_FOUND")
      return { code: error.body.code, message: t.errorNotFound };
    if (error.body.code === "ALREADY_CHECKED_IN")
      return { code: error.body.code, message: t.errorAlready };
    if (error.body.code === "BOOKING_NOT_AVAILABLE_FOR_CHECKIN")
      return { code: error.body.code, message: t.errorBookingUnavailable };
    if (error.body.code === "NOT_CHECKED_IN")
      return { code: error.body.code, message: t.notCheckedIn };
    if (error.body.code === "VALIDATION_ERROR")
      return { code: error.body.code, message: t.errorValidation };
    if (
      error.body.status === 503 ||
      error.body.code === "SERVICE_UNAVAILABLE" ||
      error.body.code === "MAINTENANCE"
    )
      return { code: error.body.code, message: t.errorService };
    return {
      code: error.body.code,
      message: error.body.message || t.errorUnknown,
    };
  }
  if (error instanceof ApiTimeoutError)
    return { code: "REQUEST_TIMEOUT", message: t.errorService };
  return { code: "REQUEST_FAILED", message: t.errorUnknown };
}
