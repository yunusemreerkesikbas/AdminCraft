"use client";

import { useEffect } from "react";

type SetLangProps = {
  locale: string;
};

export function SetLang({ locale }: SetLangProps) {
  useEffect(() => {
    if (typeof document !== "undefined") {
      document.documentElement.lang = locale;
    }
  }, [locale]);
  return null;
}
