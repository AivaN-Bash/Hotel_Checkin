import { Card } from "@heroui/react";
import type { CardProps } from "@heroui/react";
import { cardVariants, tv, type VariantProps } from "@heroui/styles";

const stepCardStyles = tv({
  extend: cardVariants,
  base: "step-card",
  variants: {
    density: {
      comfortable: "step-card--comfortable",
      compact: "step-card--compact",
    },
  },
  defaultVariants: { density: "comfortable" },
});

type StepCardProps = CardProps & VariantProps<typeof stepCardStyles>;

export function StepCard({ density, className, ...props }: StepCardProps) {
  return (
    <Card
      {...props}
      className={stepCardStyles({ density, className }).base()}
    />
  );
}
