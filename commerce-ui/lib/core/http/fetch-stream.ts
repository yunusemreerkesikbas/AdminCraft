import type { NextRequest } from "next/server";
import { buildForwardHeaders } from "./headers";
import { buildCommerceUrl } from "./query";
import { createRequestTimeoutSignal } from "./request-timeout";

export type FetchStreamOptions = {
  cache?: RequestCache;
  responseHeaderNames?: string[];
  timeoutMs?: number;
};

const DEFAULT_RESPONSE_HEADERS = [
  "content-type",
  "content-length",
  "cache-control",
  "etag",
  "last-modified",
  "content-disposition",
  "accept-ranges",
  "content-range",
];

export const fetchCommerceStream = async (
  request: NextRequest,
  path: string,
  options?: FetchStreamOptions,
): Promise<Response> => {
  const upstreamUrl = new URL(buildCommerceUrl(path));
  request.nextUrl.searchParams.forEach((value, key) => {
    upstreamUrl.searchParams.append(key, value);
  });

  const upstream = await fetch(upstreamUrl, {
    headers: buildForwardHeaders(request),
    cache: options?.cache ?? "no-store",
    signal: createRequestTimeoutSignal(options?.timeoutMs),
  });

  const responseHeaders = new Headers();

  for (const name of options?.responseHeaderNames ?? DEFAULT_RESPONSE_HEADERS) {
    const value = upstream.headers.get(name);
    if (value) {
      responseHeaders.set(name, value);
    }
  }

  return new Response(upstream.body, {
    status: upstream.status,
    headers: responseHeaders,
  });
};
