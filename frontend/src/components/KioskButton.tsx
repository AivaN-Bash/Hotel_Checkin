import { Button } from "@heroui/react";
import type { ButtonProps } from "@heroui/react";
import { buttonVariants, tv, type VariantProps } from "@heroui/styles";

const kioskButtonStyles = tv({
  extend: buttonVariants,
  base: "kiosk-button",
  variants: { touch: { true: "kiosk-button--touch" } },
  defaultVariants: { touch: true },
});

type KioskButtonProps = ButtonProps & VariantProps<typeof kioskButtonStyles>;

export function KioskButton({ touch, className, ...props }: KioskButtonProps) {
  const extraClass = typeof className === "string" ? className : "";
  return (
    <Button
      {...props}
      className={`${kioskButtonStyles({ touch })} ${extraClass}`.trim()}
    />
  );
}
