import { ProgressBar } from "@heroui/react";
import type { KioskStep } from "../types/kiosk";

const ORDER: readonly KioskStep[] = [
  "lookup",
  "confirm",
  "verification",
  "deposit",
  "assigned",
  "success",
];

interface StepProgressProps {
  step: KioskStep;
  labels: string[];
}

export function StepProgress({ step, labels }: StepProgressProps) {
  const currentIndex = Math.max(0, ORDER.indexOf(step));
  const value =
    step === "success"
      ? 100
      : Math.round(((currentIndex + 1) / ORDER.length) * 100);
  return (
    <div
      className="progress-wrap"
      role="group"
      aria-label={`Step ${currentIndex + 1} of ${ORDER.length}`}
    >
      <div className="progress-labels" aria-hidden="true">
        {labels.map((label, index) => (
          <span
            key={label}
            className={
              index === currentIndex
                ? "progress-label is-current"
                : index < currentIndex
                  ? "progress-label is-complete"
                  : "progress-label"
            }
          >
            <span className="progress-dot">
              {index < currentIndex ? "✓" : index + 1}
            </span>
            {label}
          </span>
        ))}
      </div>
      <ProgressBar
        aria-label={`Progress: ${labels[currentIndex]}`}
        value={value}
        className="kiosk-progress"
      >
        <ProgressBar.Track>
          <ProgressBar.Fill />
        </ProgressBar.Track>
      </ProgressBar>
    </div>
  );
}
