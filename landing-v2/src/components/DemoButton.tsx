"use client";

import { useState } from "react";
import { useLocale } from "next-intl";
import { DemoRequestModal } from "@/components/modals/DemoRequestModal";

type Props = {
  children: React.ReactNode;
  className?: string;
};

/**
 * Drop-in replacement for any "Demo Talep Et" anchor/button.
 * Manages its own modal state; safe to use from server components.
 */
export function DemoButton({ children, className }: Props) {
  const locale = useLocale();
  const [open, setOpen] = useState(false);

  return (
    <>
      <button type="button" onClick={() => setOpen(true)} className={className}>
        {children}
      </button>
      <DemoRequestModal open={open} onClose={() => setOpen(false)} locale={locale} />
    </>
  );
}
