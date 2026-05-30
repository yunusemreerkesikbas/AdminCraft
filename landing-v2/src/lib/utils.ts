import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/** Normalizes a route locale to the two-letter tag used for API + analytics. */
export function toLocaleTag(locale: string): "tr" | "en" {
  return locale === "tr" ? "tr" : "en"
}
