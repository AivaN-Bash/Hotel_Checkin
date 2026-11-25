import { act, renderHook } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { useIdleTimer } from "./useIdleTimer";

afterEach(() => vi.useRealTimers());

describe("useIdleTimer", () => {
  it("warns before reset and pauses while disabled", () => {
    vi.useFakeTimers();
    const onReset = vi.fn();
    const { result, rerender } = renderHook(
      ({ enabled }) => useIdleTimer(enabled, onReset),
      { initialProps: { enabled: true } },
    );
    act(() => {
      vi.advanceTimersByTime(45_000);
    });
    expect(result.current.isWarning).toBe(true);
    expect(result.current.secondsRemaining).toBe(15);
    rerender({ enabled: false });
    act(() => {
      vi.advanceTimersByTime(60_000);
    });
    expect(onReset).not.toHaveBeenCalled();
    rerender({ enabled: true });
    act(() => {
      vi.advanceTimersByTime(60_000);
    });
    expect(onReset).toHaveBeenCalledTimes(1);
  });
});
