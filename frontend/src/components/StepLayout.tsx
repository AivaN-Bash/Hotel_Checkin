import { Button, Card } from "@heroui/react";
import { ArrowLeft, Moon, Sun } from "lucide-react";
import { copy } from "../i18n/kiosk";
import type { KioskMode, KioskStep } from "../types/kiosk";
import type { Language } from "../types/service";
import { KioskButton } from "./KioskButton";
import { StepCard } from "./StepCard";
import { StepProgress } from "./StepProgress";

interface StepLayoutProps {
  step: KioskStep;
  mode?: KioskMode;
  language: Language;
  theme: "light" | "dark";
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  primaryLabel: string;
  onPrimary: () => void;
  primaryFormId?: string;
  onBack?: () => void;
  onCancel?: () => void;
  onLanguageChange: () => void;
  onThemeToggle: () => void;
  isPending?: boolean;
  primaryDisabled?: boolean;
}

export function StepLayout({
  step,
  mode = "checkin",
  language,
  theme,
  title,
  subtitle,
  children,
  primaryLabel,
  onPrimary,
  primaryFormId,
  onBack,
  onCancel,
  onLanguageChange,
  onThemeToggle,
  isPending,
  primaryDisabled,
}: StepLayoutProps) {
  const t = copy[language];
  const steps: readonly KioskStep[] =
    mode === "checkout"
      ? ["lookup", "confirm", "success"]
      : ["lookup", "confirm", "verification", "deposit", "assigned", "success"];
  const stepIndex = Math.max(0, steps.indexOf(step));
  return (
    <main className="kiosk-page">
      <header className="kiosk-header">
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
            onPress={onLanguageChange}
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
            {theme === "light" ? <Moon size={19} /> : <Sun size={19} />}
          </Button>
        </div>
      </header>
      <div className="kiosk-content">
        <StepProgress
          step={step}
          labels={(mode === "checkout" ? t.checkoutSteps : t.steps) as string[]}
          mode={mode}
        />
        <StepCard className="flow-card">
          <Card.Header className="flow-card-heading">
            <div className="step-index">
              {String(stepIndex + 1).padStart(2, "0")}{" "}
              <span>/ {String(steps.length).padStart(2, "0")}</span>
            </div>
            <h1>{title}</h1>
            {subtitle && <p>{subtitle}</p>}
          </Card.Header>
          <Card.Content className="flow-card-content">{children}</Card.Content>
          <Card.Footer className="flow-card-footer">
            <div className="footer-secondary">
              {onBack && (
                <Button variant="ghost" size="lg" onPress={onBack}>
                  <ArrowLeft size={18} aria-hidden="true" />
                  {t.back}
                </Button>
              )}
              {onCancel && (
                <Button variant="tertiary" size="lg" onPress={onCancel}>
                  {t.cancel}
                </Button>
              )}
            </div>
            <KioskButton
              variant="primary"
              size="lg"
              fullWidth
              onPress={primaryFormId ? undefined : onPrimary}
              form={primaryFormId}
              type={primaryFormId ? "submit" : "button"}
              isPending={isPending}
              isDisabled={primaryDisabled}
            >
              {primaryLabel}
            </KioskButton>
          </Card.Footer>
        </StepCard>
      </div>
      <footer className="kiosk-footer">
        <span>{t.complimentary}</span>
        <span>•</span>
        <span>{t.helpText}</span>
      </footer>
    </main>
  );
}
