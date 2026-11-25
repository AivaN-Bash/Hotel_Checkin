import { ProgressBar } from "@heroui/react";
import type { KioskMode, KioskStep } from "../types/kiosk";

const ORDER: Record<KioskMode, readonly KioskStep[]> = {
  checkin: [
    "lookup",
    "confirm",
    "verification",
    "deposit",
    "assigned",
    "success",
  ],
  checkout: ["lookup", "confirm", "success"],
};

interface StepProgressProps {
  step: KioskStep;
  labels: string[];
  mode?: KioskMode;
}

export function StepProgress({
  step,
  labels,
  mode = "checkin",
}: StepProgressProps) {
  const steps = ORDER[mode];
  const currentIndex = Math.max(0, steps.indexOf(step));
  const value =
    step === "success"
      ? 100
      : Math.round(((currentIndex + 1) / steps.length) * 100);
  return (
    <div
      className="progress-wrap"
      role="group"
      aria-label={`Step ${currentIndex + 1} of ${steps.length}`}
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
