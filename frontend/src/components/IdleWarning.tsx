import { Button, Modal } from "@heroui/react";
import { copy } from "../i18n/kiosk";
import type { Language } from "../types/service";

interface IdleWarningProps {
  open: boolean;
  secondsRemaining: number;
  language: Language;
  onStay: () => void;
  onReset: () => void;
}

export function IdleWarning({
  open,
  secondsRemaining,
  language,
  onStay,
  onReset,
}: IdleWarningProps) {
  const t = copy[language];
  return (
    <Modal>
      <Modal.Backdrop
        isOpen={open}
        isDismissable={false}
        isKeyboardDismissDisabled
      >
        <Modal.Container placement="center" size="sm">
          <Modal.Dialog
            role="alertdialog"
            aria-labelledby="idle-heading"
            aria-describedby="idle-description"
            className="idle-dialog"
          >
            <Modal.Header>
              <Modal.Heading id="idle-heading">{t.idleTitle}</Modal.Heading>
            </Modal.Header>
            <Modal.Body>
              <p id="idle-description">
                {t.idleBody}{" "}
                <strong className="tabular-nums">{secondsRemaining}</strong>{" "}
                {language === "en" ? "seconds." : "วินาที"}
              </p>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="tertiary" size="lg" onPress={onReset}>
                {t.idleReset}
              </Button>
              <Button variant="primary" size="lg" onPress={onStay}>
                {t.stay}
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
