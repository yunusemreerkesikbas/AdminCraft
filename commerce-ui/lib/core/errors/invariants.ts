import { notFound } from "next/navigation";

export const requireSiteConfig = <T>(
  site: T | null,
  message = "Site config not found.",
): T => {
  if (!site) {
    throw new Error(message);
  }

  return site;
};

export const requirePageOrNotFound = <T>(page: T | null): T => {
  if (!page) {
    notFound();
  }

  return page;
};

export const requireEntityOrNotFound = <T>(value: T | null): T => {
  if (!value) {
    notFound();
  }

  return value;
};
