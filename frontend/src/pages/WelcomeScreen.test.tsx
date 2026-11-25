import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { WelcomeScreen } from "./FlowScreens";

describe("WelcomeScreen", () => {
  it("expands the resort image on scroll while keeping both flows available", () => {
    const { container } = render(
      <WelcomeScreen
        language="en"
        theme="light"
        onLanguageChange={vi.fn()}
        onThemeToggle={vi.fn()}
        onStart={vi.fn()}
        onStartCheckout={vi.fn()}
      />,
    );
    const track = container.querySelector(".welcome-expansion-track");
    const media = container.querySelector(".welcome-art");
    if (
      !(track instanceof HTMLDivElement) ||
      !(media instanceof HTMLDivElement)
    ) {
      throw new Error("Welcome media elements were not rendered");
    }

    const viewportHeight = window.innerHeight;
    Object.defineProperty(track, "offsetHeight", {
      configurable: true,
      value: viewportHeight * 2,
    });
    vi.spyOn(track, "getBoundingClientRect").mockReturnValue(
      new DOMRect(0, -viewportHeight, 100, viewportHeight * 2),
    );

    fireEvent.scroll(window);

    expect(media).toHaveStyle({ inset: "0px", borderRadius: "0px" });
    expect(
      screen.getByRole("button", { name: "Begin check-in" }),
    ).toBeVisible();
    expect(
      screen.getByRole("button", { name: "Begin check-out" }),
    ).toBeVisible();
  });
});
