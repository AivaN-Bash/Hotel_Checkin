import { useEffect, useRef, useState, type CSSProperties } from "react";
import { Button, Input, Label, TextField } from "@heroui/react";
import {
  Check,
  CreditCard,
  KeyRound,
  ShieldCheck,
  UserRound,
} from "lucide-react";
import { QRCodeSVG } from "qrcode.react";
import { copy } from "../i18n/kiosk";
import type {
  BookingDetails,
  CheckinDetails,
  KioskMode,
  LookupMethod,
} from "../types/kiosk";
import type { Language } from "../types/service";
import { ErrorMessage } from "../components/ErrorMessage";
import { KioskButton } from "../components/KioskButton";
import { StepCard } from "../components/StepCard";
import { StepLayout } from "../components/StepLayout";
import { StepProgress } from "../components/StepProgress";

interface SharedProps {
  language: Language;
  theme: "light" | "dark";
  onLanguageChange: () => void;
  onThemeToggle: () => void;
  onBack?: () => void;
  onCancel: () => void;
}

interface LookupProps extends SharedProps {
  mode: KioskMode;
  method: LookupMethod;
  bookingCode: string;
  lastName: string;
  phoneNumber: string;
  error: { code: string; message: string } | null;
  busy: boolean;
  onMethodChange: (method: LookupMethod) => void;
  onBookingCodeChange: (value: string) => void;
  onLastNameChange: (value: string) => void;
  onPhoneChange: (value: string) => void;
  onSubmit: () => void;
}

export function BookingLookupScreen(props: LookupProps) {
  const t = copy[props.language];
  const formId = "booking-lookup-form";
  return (
    <StepLayout
      {...shared(props)}
      step="lookup"
      mode={props.mode}
      title={props.mode === "checkout" ? t.checkoutFindTitle : t.findTitle}
      subtitle={props.mode === "checkout" ? t.checkoutFindBody : t.findBody}
      primaryLabel={t.lookup}
      primaryFormId={formId}
      onPrimary={() => undefined}
      isPending={props.busy}
    >
      <div className="method-switch" role="group" aria-label={t.findTitle}>
        <Button
          variant={props.method === "code" ? "secondary" : "outline"}
          size="lg"
          aria-pressed={props.method === "code"}
          onPress={() => props.onMethodChange("code")}
        >
          <KeyRound size={18} aria-hidden="true" />
          {t.code}
        </Button>
        <Button
          variant={props.method === "guest" ? "secondary" : "outline"}
          size="lg"
          aria-pressed={props.method === "guest"}
          onPress={() => props.onMethodChange("guest")}
        >
          <UserRound size={18} aria-hidden="true" />
          {t.guest}
        </Button>
      </div>
      <form
        id={formId}
        className="kiosk-form"
        onSubmit={(event) => {
          event.preventDefault();
          props.onSubmit();
        }}
      >
        {props.method === "code" ? (
          <TextField isRequired fullWidth>
            <Label>{t.bookingCode}</Label>
            <Input
              autoComplete="off"
              maxLength={24}
              value={props.bookingCode}
              placeholder={t.codePlaceholder}
              onChange={(event) =>
                props.onBookingCodeChange(event.target.value)
              }
            />
          </TextField>
        ) : (
          <>
            <TextField isRequired fullWidth>
              <Label>{t.lastName}</Label>
              <Input
                autoComplete="family-name"
                maxLength={80}
                value={props.lastName}
                placeholder={t.lastNamePlaceholder}
                onChange={(event) => props.onLastNameChange(event.target.value)}
              />
            </TextField>
            <TextField isRequired fullWidth>
              <Label>{t.phone}</Label>
              <Input
                type="tel"
                inputMode="tel"
                autoComplete="tel"
                maxLength={32}
                value={props.phoneNumber}
                placeholder={t.phonePlaceholder}
                onChange={(event) => props.onPhoneChange(event.target.value)}
              />
            </TextField>
          </>
        )}
      </form>
      <p className="form-note">
        <ShieldCheck size={15} aria-hidden="true" />
        {t.lookupHelp}
      </p>
      <p className="form-note">
        {props.mode === "checkout" ? t.demoCheckOutHint : t.demoCheckInHint}
      </p>
      {props.error && (
        <ErrorMessage
          message={props.error.message}
          title={
            props.error.code === "BOOKING_NOT_FOUND" ? t.errorTitle : undefined
          }
        />
      )}
    </StepLayout>
  );
}

interface ConfirmProps extends SharedProps {
  booking: BookingDetails;
  onContinue: () => void;
  checkout?: boolean;
  error?: { code: string; message: string } | null;
  busy?: boolean;
}

export function ConfirmBookingScreen({
  booking,
  language,
  onContinue,
  checkout = false,
  error = null,
  busy = false,
  ...sharedProps
}: ConfirmProps) {
  const t = copy[language];
  return (
    <StepLayout
      {...shared({ ...sharedProps, language })}
      step="confirm"
      mode={checkout ? "checkout" : "checkin"}
      title={checkout ? t.checkoutTitle : t.confirmTitle}
      subtitle={checkout ? t.checkoutBody : t.confirmBody}
      primaryLabel={checkout ? t.checkoutConfirm : t.reviewContinue}
      onPrimary={onContinue}
      isPending={busy}
      onBack={sharedProps.onBack}
      onCancel={sharedProps.onCancel}
    >
      <dl className="booking-details">
        <DetailRow label={t.guestName} value={booking.guestName} />
        <DetailRow label={t.roomType} value={booking.roomType} />
        <DetailRow
          label={t.dates}
          value={formatDateRange(
            booking.checkInDate,
            booking.checkOutDate,
            language,
          )}
        />
        <DetailRow label={t.guests} value={String(booking.guestCount)} />
      </dl>
      {error && <ErrorMessage message={error.message} />}
    </StepLayout>
  );
}

interface VerificationProps extends SharedProps {
  onContinue: () => void;
}

export function VerificationScreen({
  language,
  onContinue,
  ...sharedProps
}: VerificationProps) {
  const t = copy[language];
  return (
    <StepLayout
      {...shared({ ...sharedProps, language })}
      step="verification"
      title={t.verificationTitle}
      subtitle={t.verificationBody}
      primaryLabel={t.verify}
      onPrimary={onContinue}
      onBack={sharedProps.onBack}
      onCancel={sharedProps.onCancel}
    >
      <div className="demo-step">
        <div className="demo-icon">
          <ShieldCheck size={30} strokeWidth={1.7} />
        </div>
        <p>{t.demoLabel}</p>
      </div>
    </StepLayout>
  );
}

interface DepositProps extends SharedProps {
  error: string | null;
  busy: boolean;
  onComplete: () => void;
}

export function DepositScreen({
  language,
  error,
  busy,
  onComplete,
  ...sharedProps
}: DepositProps) {
  const t = copy[language];
  return (
    <StepLayout
      {...shared({ ...sharedProps, language })}
      step="deposit"
      title={t.depositTitle}
      subtitle={t.depositBody}
      primaryLabel={t.deposit}
      onPrimary={onComplete}
      onBack={sharedProps.onBack}
      onCancel={sharedProps.onCancel}
      isPending={busy}
    >
      <div className="deposit-summary">
        <div className="deposit-icon">
          <CreditCard size={23} />
        </div>
        <strong>{t.depositAmount}</strong>
      </div>
      {error && <ErrorMessage message={error} />}
    </StepLayout>
  );
}

interface AssignedProps extends SharedProps {
  checkin: CheckinDetails;
  onContinue: () => void;
}

export function AssignedRoomScreen({
  checkin,
  language,
  onContinue,
  ...sharedProps
}: AssignedProps) {
  const t = copy[language];
  return (
    <StepLayout
      {...shared({ ...sharedProps, language })}
      step="assigned"
      title={t.assignedTitle}
      subtitle={t.assignedBody}
      primaryLabel={t.assignedContinue}
      onPrimary={onContinue}
      onCancel={sharedProps.onCancel}
    >
      <div className="room-key-layout">
        <div className="room-number-panel">
          <span>{t.roomNumber}</span>
          <strong>{checkin.roomNumber}</strong>
          <small>
            {t.floor} {checkin.floor}
          </small>
        </div>
        <div className="qr-panel">
          <QRCodeSVG
            value={checkin.digitalKey}
            size={164}
            level="M"
            includeMargin
            aria-label={t.digitalKey}
          />
          <span>{t.digitalKey}</span>
        </div>
      </div>
    </StepLayout>
  );
}

interface SuccessProps {
  language: Language;
  onFinish: () => void;
  checkedOut?: boolean;
}

export function SuccessScreen({
  language,
  onFinish,
  checkedOut = false,
}: SuccessProps) {
  const t = copy[language];
  return (
    <main className="success-page">
      <header className="success-topline">
        <span className="wordmark-icon">A</span>
        <span>{t.hotel}</span>
      </header>
      <div className="success-progress">
        <StepProgress
          step="success"
          labels={(checkedOut ? t.checkoutSteps : t.steps) as string[]}
          mode={checkedOut ? "checkout" : "checkin"}
        />
      </div>
      <StepCard className="success-card">
        <div className="success-check">
          <Check size={34} aria-hidden="true" />
        </div>
        <p className="success-eyebrow">{t.complimentary}</p>
        <h1>{checkedOut ? t.checkoutSuccessTitle : t.successTitle}</h1>
        <p>{checkedOut ? t.checkoutSuccessBody : t.successBody}</p>
        <p className="success-reset-note">{t.autoReset}</p>
        <KioskButton variant="primary" size="lg" onPress={onFinish}>
          {t.successDone}
        </KioskButton>
      </StepCard>
    </main>
  );
}

interface WelcomeProps {
  language: Language;
  theme: "light" | "dark";
  onLanguageChange: (language: Language) => void;
  onThemeToggle: () => void;
  onStart: () => void;
  onStartCheckout: () => void;
}

export function WelcomeScreen({
  language,
  theme,
  onLanguageChange,
  onThemeToggle,
  onStart,
  onStartCheckout,
}: WelcomeProps) {
  const t = copy[language];
  const mediaTrackRef = useRef<HTMLDivElement>(null);
  const [mediaExpansion, setMediaExpansion] = useState(0);

  useEffect(() => {
    const track = mediaTrackRef.current;
    if (!track) return;

    const updateExpansion = () => {
      const scrollRange = track.offsetHeight - window.innerHeight;
      const nextExpansion =
        scrollRange > 0
          ? Math.min(
              1,
              Math.max(0, -track.getBoundingClientRect().top / scrollRange),
            )
          : 0;

      setMediaExpansion((current) =>
        Math.abs(current - nextExpansion) < 0.005 ? current : nextExpansion,
      );
    };

    updateExpansion();
    window.addEventListener("scroll", updateExpansion, { passive: true });
    window.addEventListener("resize", updateExpansion);
    return () => {
      window.removeEventListener("scroll", updateExpansion);
      window.removeEventListener("resize", updateExpansion);
    };
  }, []);

  const mediaInset = `${Math.min(window.innerWidth * 0.04, 48) * (1 - mediaExpansion)}px`;
  const mediaStyle: CSSProperties = {
    inset: mediaInset,
    borderRadius: `${28 * (1 - mediaExpansion)}px`,
  };
  const startFlow = (action: () => void) => {
    if (window.scrollY > 0) window.scrollTo(0, 0);
    action();
  };

  return (
    <main className="welcome-page">
      <header className="welcome-header">
        <a
          className="wordmark"
          href="/"
          aria-label={t.hotel}
          onClick={(event) => event.preventDefault()}
        >
          <span className="wordmark-icon">A</span>
          <span>
            <strong>{t.hotel}</strong>
            <small>{t.city}</small>
          </span>
        </a>
        <div className="header-tools">
          <Button
            variant="ghost"
            size="md"
            className="header-action"
            onPress={() => onLanguageChange(language === "en" ? "th" : "en")}
            aria-label={t.language}
          >
            {language === "en" ? "ไทย" : "English"}
          </Button>
          <Button
            variant="ghost"
            size="md"
            isIconOnly
            aria-label={t.theme}
            onPress={onThemeToggle}
          >
            {theme === "light" ? <span>◐</span> : <span>☼</span>}
          </Button>
        </div>
      </header>
      <div className="welcome-progress">
        <StepProgress step="welcome" labels={t.steps as string[]} />
      </div>
      <div className="welcome-expansion-track" ref={mediaTrackRef}>
        <section className="welcome-hero" aria-label={t.hotel}>
          <div className="welcome-art" style={mediaStyle} aria-hidden="true">
            <img src="/images/welcome-resort.jpg" alt="" fetchPriority="high" />
          </div>
          <div className="welcome-copy">
            <p className="welcome-eyebrow">{t.welcomeEyebrow}</p>
            <h1>
              {t.welcomeTitle.split("\n").map((line) => (
                <span key={line}>{line}</span>
              ))}
            </h1>
            <p className="welcome-body">{t.welcomeBody}</p>
            <KioskButton
              variant="primary"
              size="lg"
              fullWidth
              onPress={() => startFlow(onStart)}
            >
              {t.start}
              <span aria-hidden="true">→</span>
            </KioskButton>
            <KioskButton
              variant="secondary"
              size="lg"
              fullWidth
              className="welcome-checkout"
              onPress={() => startFlow(onStartCheckout)}
            >
              {t.startCheckout}
            </KioskButton>
            <div className="welcome-assurance">
              <ShieldCheck size={17} aria-hidden="true" />
              {t.secure}
            </div>
          </div>
        </section>
      </div>
      <footer className="welcome-footer">
        <span>{t.hotel}</span>
        <span>·</span>
        <span>{t.city}</span>
        <span className="welcome-help">
          {t.helpLabel} <strong>{t.helpText}</strong>
        </span>
      </footer>
    </main>
  );
}

interface DetailRowProps {
  label: string;
  value: string;
}

function DetailRow({ label, value }: DetailRowProps) {
  return (
    <div className="detail-row">
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

function shared(props: SharedProps) {
  return {
    language: props.language,
    theme: props.theme,
    onLanguageChange: props.onLanguageChange,
    onThemeToggle: props.onThemeToggle,
    ...(props.onBack ? { onBack: props.onBack } : {}),
    onCancel: props.onCancel,
  };
}

function formatDateRange(
  start: string,
  end: string,
  language: Language,
): string {
  const locale = language === "en" ? "en-GB" : "th-TH";
  const options: Intl.DateTimeFormatOptions = {
    day: "numeric",
    month: "short",
  };
  const first = new Date(`${start}T12:00:00`);
  const last = new Date(`${end}T12:00:00`);
  if (Number.isNaN(first.getTime()) || Number.isNaN(last.getTime()))
    return `${start} – ${end}`;
  return `${new Intl.DateTimeFormat(locale, options).format(first)} – ${new Intl.DateTimeFormat(locale, { ...options, year: "numeric" }).format(last)}`;
}
