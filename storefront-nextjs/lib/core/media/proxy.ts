import type { NextRequest } from "next/server";
import { fetchCmsStream } from "../http/fetch-stream";

const buildMediaPath = (segments: string[]): string =>
  `media/${segments.map((segment) => encodeURIComponent(segment)).join("/")}`;

export const proxyTenantMediaRequest = async (
  request: NextRequest,
  paramsPromise: Promise<{ path: string[] }>,
): Promise<Response> => {
  const { path } = await paramsPromise;
  return fetchCmsStream(request, buildMediaPath(path), { cache: "force-cache" });
};
