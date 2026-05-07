import type { ApiResponse } from "../../types";
import { buildJsonHeaders } from "./headers";
import { buildCmsUrl, type QueryValue } from "./query";

export type FetchJsonOptions = {
  cache?: RequestCache;
  revalidate?: number;
  extraHeaders?: Record<string, string>;
  /**
   * SmartEdit preview ticket. When set, the request bypasses ISR caching
   * (forces {@code cache: "no-store"}) and forwards {@code X-Cms-Preview-Ticket}
   * so the backend serves DRAFT content.
   */
  previewTicket?: string;
};

const isDevelopment = process.env.NODE_ENV !== "production";

export const fetchCmsJson = async <T>(
  path: string,
  params?: Record<string, QueryValue>,
  options?: FetchJsonOptions,
): Promise<T | null> => {
  const url = buildCmsUrl(path, params);
  const previewActive = Boolean(options?.previewTicket);
  const requestCache = previewActive
    ? "no-store"
    : isDevelopment
      ? "no-store"
      : options?.cache ?? "force-cache";
  const previewHeaders: Record<string, string> = previewActive
    ? { "X-Cms-Preview-Ticket": options!.previewTicket! }
    : {};
  const fetchOptions: RequestInit & { next?: { revalidate?: number } } = {
    headers: { ...(await buildJsonHeaders()), ...previewHeaders, ...options?.extraHeaders },
    cache: requestCache,
  };

  if (!previewActive && !isDevelopment && options?.revalidate !== undefined) {
    fetchOptions.next = { revalidate: options.revalidate };
  }

  const response = await fetch(url, fetchOptions);

  if (response.status === 404) {
    return null;
  }

  if (response.status === 429 || response.status >= 500) {
    console.error(`[CMS] Request failed ${response.status}: ${url}`);
    return null;
  }

  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok) {
    const message = payload?.message ?? `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  if (payload.result === "ERROR") {
    return null;
  }

  return payload.data ?? null;
};
