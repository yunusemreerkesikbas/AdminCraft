export const DEFAULT_COMMERCE_REQUEST_TIMEOUT_MS = 8000;

export const createRequestTimeoutSignal = (
  timeoutMs = DEFAULT_COMMERCE_REQUEST_TIMEOUT_MS,
): AbortSignal => {
  if ("timeout" in AbortSignal) {
    return AbortSignal.timeout(timeoutMs);
  }

  const controller = new AbortController();
  setTimeout(() => controller.abort(), timeoutMs);
  return controller.signal;
};
