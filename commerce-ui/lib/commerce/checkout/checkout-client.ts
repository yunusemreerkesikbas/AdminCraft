import { readApiResponse } from "@/lib/core/http/api-response";
import { resolveCommerceEndpoint } from "@/lib/core/http/endpoints";
import { createRequestTimeoutSignal } from "@/lib/core/http/request-timeout";
import type {
  CheckoutAddressSelectionRequest,
  CheckoutResponse,
} from "./types";

export type CommerceCheckoutClientConfig = {
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
};

type CheckoutRequestOptions = {
  method?: "GET" | "POST" | "PATCH";
  body?: unknown;
  accessToken: string;
};

export type CommerceCheckoutClient = {
  startCheckout: (
    accessToken: string,
    request?: CheckoutAddressSelectionRequest,
  ) => Promise<CheckoutResponse>;
  getCurrentCheckout: (accessToken: string) => Promise<CheckoutResponse>;
  updateCheckoutAddresses: (
    accessToken: string,
    checkoutUid: string,
    request: CheckoutAddressSelectionRequest,
  ) => Promise<CheckoutResponse>;
};

const buildCheckoutUrl = (apiBaseUrl: string, path: string): string => {
  const normalizedBase = apiBaseUrl.replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;

  return `${normalizedBase}/${normalizedPath}`;
};

export const createCommerceCheckoutClient = ({
  apiBaseUrl,
  lang,
  tenantHeaders,
}: CommerceCheckoutClientConfig): CommerceCheckoutClient => {
  const requestCheckout = async (
    path: string,
    options: CheckoutRequestOptions,
  ): Promise<CheckoutResponse> => {
    const headers = new Headers({
      Accept: "application/json",
      "Accept-Language": lang,
      Authorization: `Bearer ${options.accessToken}`,
      ...tenantHeaders,
    });

    if (options.body !== undefined) {
      headers.set("Content-Type", "application/json");
    }

    const response = await fetch(buildCheckoutUrl(apiBaseUrl, path), {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      cache: "no-store",
      signal: createRequestTimeoutSignal(),
    });

    const payload = await readApiResponse<CheckoutResponse>(response, "");

    if (payload.result === "ERROR" || !payload.data) {
      throw new Error(payload.message ?? "");
    }

    return payload.data;
  };

  return {
    startCheckout: (accessToken, checkoutRequest) =>
      requestCheckout(resolveCommerceEndpoint("checkout"), {
        method: "POST",
        body: checkoutRequest,
        accessToken,
      }),
    getCurrentCheckout: (accessToken) =>
      requestCheckout(resolveCommerceEndpoint("currentCheckout"), {
        accessToken,
      }),
    updateCheckoutAddresses: (accessToken, checkoutUid, checkoutRequest) =>
      requestCheckout(resolveCommerceEndpoint("checkoutAddresses", { checkoutUid }), {
        method: "PATCH",
        body: checkoutRequest,
        accessToken,
      }),
  };
};
