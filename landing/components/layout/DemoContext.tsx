"use client";

import { createContext, useContext, useState, ReactNode } from "react";
import { DemoRequestModal } from "@/components/modals/DemoRequestModal";

type ContactContextType = { openContact: () => void };

const ContactContext = createContext<ContactContextType | undefined>(undefined);

export function useDemoContext() {
  const ctx = useContext(ContactContext);
  if (!ctx) {
    throw new Error("useDemoContext must be used within a DemoProvider");
  }
  return ctx;
}

export function DemoProvider({ children, locale }: { children: ReactNode; locale: string }) {
  const [open, setOpen] = useState(false);
  return (
    <ContactContext.Provider value={{ openContact: () => setOpen(true) }}>
      {children}
      <DemoRequestModal open={open} onClose={() => setOpen(false)} locale={locale} />
    </ContactContext.Provider>
  );
}
