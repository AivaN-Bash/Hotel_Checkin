import { useState } from "react";
import { Button } from "@heroui/react";
import { ErrorMessage } from "../components/ErrorMessage";
import { KioskButton } from "../components/KioskButton";
import { MaintenanceScreen } from "../components/MaintenanceScreen";
import { StepCard } from "../components/StepCard";
import { StepProgress } from "../components/StepProgress";
import { Loader } from "../components/Loader";
import type { Language } from "../types/service";

export function ComponentPreviewPage() {
  const [language, setLanguage] = useState<Language>("en");
  const [showMaintenance, setShowMaintenance] = useState(() =>
    new URLSearchParams(window.location.search).has("maintenance"),
  );
  if (showMaintenance)
    return (
      <MaintenanceScreen
        language={language}
        message="A preview of the scheduled maintenance state."
        onRetry={() => setShowMaintenance(false)}
      />
    );
  return (
    <main className="preview-page">
      <header className="preview-header">
        <div>
          <p className="welcome-eyebrow">THE ASTER · UI PREVIEW</p>
          <h1>Kiosk components</h1>
        </div>
        <Button
          variant="outline"
          size="lg"
          onPress={() => setLanguage(language === "en" ? "th" : "en")}
        >
          {language === "en" ? "ไทย" : "English"}
        </Button>
      </header>
      <section className="preview-grid">
        <StepCard className="preview-card">
          <h2>Touch buttons</h2>
          <div className="preview-row">
            <KioskButton variant="primary" size="lg">
              Primary action
            </KioskButton>
            <KioskButton variant="secondary" size="lg">
              Secondary
            </KioskButton>
            <KioskButton variant="tertiary" size="lg">
              Start over
            </KioskButton>
          </div>
        </StepCard>
        <StepCard className="preview-card">
          <h2>Progress</h2>
          <StepProgress
            step="verification"
            labels={[
              "Find booking",
              "Your stay",
              "ID check",
              "Deposit",
              "Your room",
              "Complete",
            ]}
          />
        </StepCard>
        <StepCard className="preview-card">
          <h2>States</h2>
          <ErrorMessage message="We could not find this demo reservation." />
          <div className="preview-loader">
            <Loader label="Looking up your booking" />
          </div>
        </StepCard>
        <StepCard className="preview-card">
          <h2>Maintenance</h2>
          <p>
            See the full static maintenance screen in both supported languages.
          </p>
          <Button
            variant="secondary"
            size="lg"
            onPress={() => setShowMaintenance(true)}
          >
            Preview maintenance screen
          </Button>
        </StepCard>
      </section>
    </main>
  );
}
