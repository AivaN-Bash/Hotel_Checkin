import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { WelcomeScreen } from "./FlowScreens";

describe("WelcomeScreen", () => {
  it("keeps the welcome hero static and both booking actions available", () => {
    const onStart = vi.fn();
    const onStartCheckout = vi.fn();
    const { container } = render(
      <WelcomeScreen
        language="en"
        theme="light"
        onLanguageChange={vi.fn()}
        onThemeToggle={vi.fn()}
        onStart={onStart}
        onStartCheckout={onStartCheckout}
      />,
    );
    expect(container.querySelector(".welcome-expansion-track")).toBeNull();
    fireEvent.click(screen.getByRole("button", { name: "Begin check-in" }));
    fireEvent.click(screen.getByRole("button", { name: "Begin check-out" }));
    expect(onStart).toHaveBeenCalledOnce();
    expect(onStartCheckout).toHaveBeenCalledOnce();
  });
});
