import { CircleAlert } from "lucide-react";

interface ErrorMessageProps {
  message: string;
  title?: string;
}

export function ErrorMessage({ message, title }: ErrorMessageProps) {
  return (
    <div className="error-message" role="alert">
      <CircleAlert size={20} aria-hidden="true" />
      <span>
        {title && <strong>{title} </strong>}
        {message}
      </span>
    </div>
  );
}
