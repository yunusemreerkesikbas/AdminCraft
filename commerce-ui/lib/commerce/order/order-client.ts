import { readApiResponse } from "@/lib/core/http/api-response";
import { resolveCommerceEndpoint } from "@/lib/core/http/endpoints";
import { createRequestTimeoutSignal } from "@/lib/core/http/request-timeout";
import type {
  CommerceOrderDetailResponse,
  CommerceOrderResolutionRequestResponse,
  CreateCommerceOrderResolutionRequestPayload,
  CommerceOrderSummaryResponse,
  PageableResponse,
} from "./types";

export type CommerceOrderClientConfig = {
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
};

type OrderRequestOptions = {
  accessToken: string;
  params?: Record<string, string | number | boolean | null | undefined>;
};

export type CommerceOrderClient = {
  listOrders: (
    accessToken: string,
    params?: OrderRequestOptions["params"],
  ) => Promise<PageableResponse<CommerceOrderSummaryResponse>>;
  getOrder: (
    accessToken: string,
    orderUid: string,
  ) => Promise<CommerceOrderDetailResponse>;
  listOrderRequests: (
    accessToken: string,
    orderUid: string,
  ) => Promise<CommerceOrderResolutionRequestResponse[]>;
  createOrderRequest: (
    accessToken: string,
    orderUid: string,
    payload: CreateCommerceOrderResolutionRequestPayload,
  ) => Promise<CommerceOrderResolutionRequestResponse>;
};

const buildOrderUrl = (
  apiBaseUrl: string,
  path: string,
  params?: OrderRequestOptions["params"],
): string => {
  const normalizedBase = apiBaseUrl.replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;
  const url = new URL(normalizedPath, `${normalizedBase}/`);

  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null && `${value}`.length > 0) {
        url.searchParams.set(key, String(value));
      }
    }
  }

  return url.toString();
};

export const createCommerceOrderClient = ({
  apiBaseUrl,
  lang,
  tenantHeaders,
}: CommerceOrderClientConfig): CommerceOrderClient => {
  const requestOrder = async <T>(
    path: string,
    options: OrderRequestOptions,
  ): Promise<T> => {
    const response = await fetch(buildOrderUrl(apiBaseUrl, path, options.params), {
      method: "GET",
      headers: {
        Accept: "application/json",
        "Accept-Language": lang,
        Authorization: `Bearer ${options.accessToken}`,
        ...tenantHeaders,
      },
      cache: "no-store",
      signal: createRequestTimeoutSignal(),
    });

    const payload = await readApiResponse<T>(response, "");

    if (payload.result === "ERROR" || !payload.data) {
      throw new Error(payload.message ?? "");
    }

    return payload.data;
  };

  const mutateOrder = async <T>(
    path: string,
    accessToken: string,
    body: unknown,
  ): Promise<T> => {
    const response = await fetch(buildOrderUrl(apiBaseUrl, path), {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Accept-Language": lang,
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
        ...tenantHeaders,
      },
      body: JSON.stringify(body),
      cache: "no-store",
      signal: createRequestTimeoutSignal(),
    });

    const payload = await readApiResponse<T>(response, "");

    if (payload.result === "ERROR" || !payload.data) {
      throw new Error(payload.message ?? "");
    }

    return payload.data;
  };

  return {
    listOrders: (accessToken, params) =>
      requestOrder<PageableResponse<CommerceOrderSummaryResponse>>(
        resolveCommerceEndpoint("orders"),
        { accessToken, params: { page: 0, size: 20, ...params } },
      ),
    getOrder: (accessToken, orderUid) =>
      requestOrder<CommerceOrderDetailResponse>(
        resolveCommerceEndpoint("orderByUid", { orderUid }),
        { accessToken },
      ),
    listOrderRequests: (accessToken, orderUid) =>
      requestOrder<CommerceOrderResolutionRequestResponse[]>(
        resolveCommerceEndpoint("orderRequests", { orderUid }),
        { accessToken },
      ),
    createOrderRequest: (accessToken, orderUid, payload) =>
      mutateOrder<CommerceOrderResolutionRequestResponse>(
        resolveCommerceEndpoint("orderRequests", { orderUid }),
        accessToken,
        payload,
      ),
  };
};
