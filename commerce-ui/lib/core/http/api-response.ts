export type ApiResponse<T> = {
  result: "SUCCESS" | "ERROR";
  message?: string;
  data?: T;
};

export const readApiResponse = async <T>(
  response: Response,
  fallbackMessage: string,
): Promise<ApiResponse<T>> => {
  const body = await response.text();
  const fallback =
    fallbackMessage || `Request failed with status ${response.status}`;
  let payload: ApiResponse<T> | null = null;

  if (body.trim()) {
    try {
      payload = JSON.parse(body) as ApiResponse<T>;
    } catch {
      payload = null;
    }
  }

  if (!response.ok) {
    throw new Error(payload?.message ?? fallback);
  }

  if (!payload) {
    throw new Error(fallbackMessage);
  }

  return payload;
};
