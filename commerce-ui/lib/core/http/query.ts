import { getCommerceBaseUrl } from "../config/runtime-env";

export type QueryValue = string | number | boolean | null | undefined;

export const buildCommerceUrl = (
  path: string,
  params?: Record<string, QueryValue>,
): string => {
  const baseUrl = getCommerceBaseUrl();
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;
  const url = new URL(normalizedPath, `${baseUrl}/`);

  if (!params) {
    return url.toString();
  }

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && `${value}`.length > 0) {
      url.searchParams.set(key, String(value));
    }
  }

  return url.toString();
};
