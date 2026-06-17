import type { NextRequest } from "next/server";
import { getTenantHeaders, getTenantHeadersAsync } from "../config/runtime-env";

export const buildJsonHeaders = async (): Promise<HeadersInit> => ({
  Accept: "application/json",
  ...(await getTenantHeadersAsync()),
});

export const buildForwardHeaders = (
  request: NextRequest,
  names: string[] = ["accept"],
): Headers => {
  const headers = new Headers();

  for (const name of names) {
    const value = request.headers.get(name);
    if (value) {
      headers.set(name, value);
    }
  }

  for (const [name, value] of Object.entries(getTenantHeaders())) {
    headers.set(name, value);
  }

  return headers;
};
