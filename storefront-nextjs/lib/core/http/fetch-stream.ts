import type { NextRequest } from "next/server";
import { buildForwardHeaders } from "./headers";
import { buildCmsUrl } from "./query";

export type FetchStreamOptions = {
  cache?: RequestCache;
  responseHeaderNames?: string[];
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

export const fetchCmsStream = async (
  request: NextRequest,
  path: string,
  options?: FetchStreamOptions,
): Promise<Response> => {
  const upstreamUrl = new URL(buildCmsUrl(path));
  request.nextUrl.searchParams.forEach((value, key) => {
    upstreamUrl.searchParams.append(key, value);
  });

  const upstream = await fetch(upstreamUrl, {
    headers: buildForwardHeaders(request),
    cache: options?.cache ?? "force-cache",
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
