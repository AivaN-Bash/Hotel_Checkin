import { Spinner } from "@heroui/react";

interface LoaderProps {
  label: string;
}

export function Loader({ label }: LoaderProps) {
  return (
    <span className="loader-inline">
      <Spinner size="sm" aria-label={label} />
      {label}
    </span>
  );
}
