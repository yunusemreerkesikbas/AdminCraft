import { getCmsBaseUrl } from "../config/runtime-env";

export const buildMediaUrl = (path?: string | null): string => {
  if (!path) {
    return "";
  }

  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }

  const mediaPrefix = "/api/media/";
  if (path.startsWith(mediaPrefix)) {
    return `/cms-media/${path.slice(mediaPrefix.length)}`;
  }

  return `${getCmsBaseUrl()}/${path}`;
};
