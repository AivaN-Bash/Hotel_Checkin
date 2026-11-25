import { useState } from "react";
import { Button, Card, ProgressBar } from "@heroui/react";
import { RefreshCw, ConciergeBell, Sparkle } from "lucide-react";
import { maintenanceCopy } from "../i18n/maintenance";
import type { Language } from "../types/service";

interface MaintenanceScreenProps {
  language: Language;
  message?: string;
  estimatedBackAt?: string | null;
  lastCheckedAt?: string | null;
  onRetry: () => void;
  onLanguageChange?: () => void;
}

export function MaintenanceScreen({
  language,
  message,
  estimatedBackAt,
  lastCheckedAt,
  onRetry,
  onLanguageChange,
}: MaintenanceScreenProps) {
  const copy = maintenanceCopy[language];
  const [isRetrying, setIsRetrying] = useState(false);
  const retry = () => {
    setIsRetrying(true);
    onRetry();
    window.setTimeout(() => setIsRetrying(false), 1_000);
  };

  return (
    <main className="maintenance-shell" aria-labelledby="maintenance-title">
      <div className="maintenance-topline">
        <span className="brand-mark" aria-hidden="true">
          <Sparkle size={20} />
        </span>
        <span>THE ASTER</span>
        <span className="topline-divider" aria-hidden="true" />
        <span>{language === "en" ? "BANGKOK" : "กรุงเทพฯ"}</span>
        {onLanguageChange && (
          <Button
            className="maintenance-language-control"
            variant="ghost"
            size="md"
            onPress={onLanguageChange}
            aria-label={language === "en" ? "เปลี่ยนภาษา" : "Change language"}
          >
            {language === "en" ? "ไทย" : "English"}
          </Button>
        )}
      </div>
      <Card className="maintenance-card">
        <div className="maintenance-emblem" aria-hidden="true">
          <ConciergeBell size={35} strokeWidth={1.6} />
        </div>
        <p className="maintenance-eyebrow">{copy.eyebrow}</p>
        <h1 id="maintenance-title">{copy.title}</h1>
        <p className="maintenance-message">{message || copy.body}</p>
        {estimatedBackAt && (
          <p className="maintenance-return">
            {copy.returning}:{" "}
            <strong>{formatDate(estimatedBackAt, language)}</strong>
          </p>
        )}
        <div className="maintenance-help">
          <strong>{copy.help}</strong>
          <span>{copy.contact}</span>
        </div>
        <Button
          variant="primary"
          size="lg"
          fullWidth
          isPending={isRetrying}
          onPress={retry}
        >
          <RefreshCw size={19} aria-hidden="true" />
          {copy.retry}
        </Button>
        <p className="maintenance-last-checked">
          {copy.checked}:{" "}
          <time>
            {lastCheckedAt ? formatDate(lastCheckedAt, language) : "—"}
          </time>
        </p>
      </Card>
      <div className="maintenance-footer">
        <span>HOSPITALITY, THOUGHTFULLY</span>
        <span>EST. 2024</span>
      </div>
    </main>
  );
}

function formatDate(value: string, language: Language): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(language === "en" ? "en-GB" : "th-TH", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

interface WakingScreenProps {
  language: Language;
  onLanguageChange?: () => void;
}

export function WakingScreen({
  language,
  onLanguageChange,
}: WakingScreenProps) {
  return (
    <main className="maintenance-shell" aria-live="polite">
      <Card className="maintenance-card waking-card">
        {onLanguageChange && (
          <Button
            className="waking-language-control"
            variant="ghost"
            size="md"
            onPress={onLanguageChange}
            aria-label={language === "en" ? "Change language" : "เปลี่ยนภาษา"}
          >
            {language === "en" ? "ไทย" : "English"}
          </Button>
        )}
        <span className="brand-monogram" aria-hidden="true">
          A
        </span>
        <p className="maintenance-eyebrow">THE ASTER HOTEL</p>
        <h1>
          {language === "en" ? "Preparing your welcome." : "กำลังเตรียมต้อนรับคุณ"}
        </h1>
        <p className="maintenance-message">
          {language === "en"
            ? "Waking up the server. This can take up to a minute."
            : "กำลังเริ่มระบบ อาจใช้เวลาประมาณหนึ่งนาที"}
        </p>
        <ProgressBar
          aria-label={
            language === "en" ? "Waking up the server" : "กำลังเริ่มระบบ"
          }
          isIndeterminate
        />
      </Card>
    </main>
  );
}
