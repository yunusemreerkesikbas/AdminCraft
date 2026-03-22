import { getTenantHeaders } from "../config/runtime-env";
import { buildCmsUrl, type QueryValue } from "./query";

export type FetchTextOptions = {
  cache?: RequestCache;
  revalidate?: number;
  accept?: string;
};

const isDevelopment = process.env.NODE_ENV !== "production";

export const fetchCmsText = async (
  path: string,
  params?: Record<string, QueryValue>,
  options?: FetchTextOptions,
): Promise<string | null> => {
  const requestCache = isDevelopment ? "no-store" : options?.cache ?? "force-cache";
  const fetchOptions: RequestInit & { next?: { revalidate?: number } } = {
    headers: {
      Accept: options?.accept ?? "text/plain",
      ...getTenantHeaders(),
    },
    cache: requestCache,
  };

  if (!isDevelopment && options?.revalidate !== undefined) {
    fetchOptions.next = { revalidate: options.revalidate };
  }

  const response = await fetch(buildCmsUrl(path, params), fetchOptions);
  if (!response.ok) {
    return null;
  }

  return response.text();
};
