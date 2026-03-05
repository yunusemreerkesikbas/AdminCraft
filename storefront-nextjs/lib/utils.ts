export const getCmsBaseUrl = (): string => {
  const base = process.env.NEXT_PUBLIC_CMS_API_URL ?? "http://localhost:8080/api";
  return base.replace(/\/$/, "");
};

export const buildMediaUrl = (path?: string | null): string => {
  if (!path) {
    return "";
  }
  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }
  const base = getCmsBaseUrl();
  return path.startsWith("/") ? `${base}${path}` : `${base}/${path}`;
};
