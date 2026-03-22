export const safeJsonLd = (data: unknown): string =>
  JSON.stringify(data).replace(/</g, "\\u003c");
