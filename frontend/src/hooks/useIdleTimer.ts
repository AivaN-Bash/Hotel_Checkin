import { useEffect, useState } from "react";

const IDLE_RESET_SECONDS = 60;
const WARNING_SECONDS = 15;
const TICK_MS = 1_000;

export interface IdleTimerState {
  secondsRemaining: number;
  isWarning: boolean;
}

export function useIdleTimer(
  enabled: boolean,
  onReset: () => void,
): IdleTimerState {
  const [secondsRemaining, setSecondsRemaining] = useState(IDLE_RESET_SECONDS);

  useEffect(() => {
    if (!enabled) return;
    let lastActivity = Date.now();
    const registerActivity = () => {
      lastActivity = Date.now();
    };
    const interval = window.setInterval(() => {
      const remaining = Math.max(
        0,
        IDLE_RESET_SECONDS - Math.floor((Date.now() - lastActivity) / 1_000),
      );
      setSecondsRemaining(remaining);
      if (remaining === 0) {
        lastActivity = Date.now();
        setSecondsRemaining(IDLE_RESET_SECONDS);
        onReset();
      }
    }, TICK_MS);
    window.addEventListener("pointerdown", registerActivity, { passive: true });
    window.addEventListener("keydown", registerActivity);
    window.addEventListener("touchstart", registerActivity, { passive: true });
    return () => {
      window.clearInterval(interval);
      window.removeEventListener("pointerdown", registerActivity);
      window.removeEventListener("keydown", registerActivity);
      window.removeEventListener("touchstart", registerActivity);
    };
  }, [enabled, onReset]);

  useEffect(() => {
    if (enabled) setSecondsRemaining(IDLE_RESET_SECONDS);
  }, [enabled]);

  return {
    secondsRemaining,
    isWarning: enabled && secondsRemaining <= WARNING_SECONDS,
  };
}
