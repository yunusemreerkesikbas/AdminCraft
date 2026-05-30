"use client";

import { useState } from "react";
import { useLocale } from "next-intl";
import { DemoRequestModal } from "@/components/modals/DemoRequestModal";
import { track } from "@/lib/analytics";

type Props = {
  children: React.ReactNode;
  className?: string;
  /** Analytics attribution for where the demo flow was opened from. */
  location?: string;
};

/**
 * Drop-in replacement for any "Demo Talep Et" anchor/button.
 * Manages its own modal state; safe to use from server components.
 */
export function DemoButton({ children, className, location = "cta_button" }: Props) {
  const locale = useLocale();
  const [open, setOpen] = useState(false);

  return (
    <>
      <button
        type="button"
        onClick={() => {
          track("demo_open", { location });
          setOpen(true);
        }}
        className={className}
      >
        {children}
      </button>
      <DemoRequestModal open={open} onClose={() => setOpen(false)} locale={locale} />
    </>
  );
}
