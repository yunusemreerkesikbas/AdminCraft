"use client";

import { createContext, useContext, useState, ReactNode } from "react";
import { ContactModal } from "@/components/modals/DemoRequestModal";

type ContactContextType = { openContact: () => void };

const ContactContext = createContext<ContactContextType>({ openContact: () => {} });

export function useDemoContext() {
  return useContext(ContactContext);
}

export function DemoProvider({ children, locale }: { children: ReactNode; locale: string }) {
  const [open, setOpen] = useState(false);
  return (
    <ContactContext.Provider value={{ openContact: () => setOpen(true) }}>
      {children}
      <ContactModal open={open} onClose={() => setOpen(false)} locale={locale} />
    </ContactContext.Provider>
  );
}
