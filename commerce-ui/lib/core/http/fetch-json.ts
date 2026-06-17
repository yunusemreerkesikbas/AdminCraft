import type { ApiResponse } from "./api-response";
import { buildJsonHeaders } from "./headers";
import { buildCommerceUrl, type QueryValue } from "./query";
import { createRequestTimeoutSignal } from "./request-timeout";

export type FetchJsonOptions = {
  cache?: RequestCache;
  revalidate?: number;
  extraHeaders?: Record<string, string>;
  timeoutMs?: number;
};

const isDevelopment = process.env.NODE_ENV !== "production";

export const fetchCommerceJson = async <T>(
  path: string,
  params?: Record<string, QueryValue>,
  options?: FetchJsonOptions,
): Promise<T | null> => {
  const url = buildCommerceUrl(path, params);
  const requestCache = isDevelopment
    ? "no-store"
    : options?.cache ?? "force-cache";
  const fetchOptions: RequestInit & { next?: { revalidate?: number } } = {
    headers: { ...(await buildJsonHeaders()), ...options?.extraHeaders },
    cache: requestCache,
    signal: createRequestTimeoutSignal(options?.timeoutMs),
  };

  if (!isDevelopment && options?.revalidate !== undefined) {
    fetchOptions.next = { revalidate: options.revalidate };
  }

  const response = await fetch(url, fetchOptions);

  if (response.status === 404) {
    return null;
  }

  if (response.status === 429 || response.status >= 500) {
    const safePath = path.startsWith("/") ? path : `/${path}`;
    console.error(`[commerce-ui] Request failed ${response.status}: ${safePath}`);
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
