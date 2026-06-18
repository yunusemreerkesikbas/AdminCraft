import { readApiResponse } from "@/lib/core/http/api-response";
import { resolveCommerceEndpoint } from "@/lib/core/http/endpoints";
import { createRequestTimeoutSignal } from "@/lib/core/http/request-timeout";
import type {
  CreatePaymentAttemptRequest,
  PaymentAttemptResponse,
  PaymentInitializeResponse,
} from "./types";

export type CommercePaymentClientConfig = {
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
};

type PaymentRequestOptions = {
  method?: "GET" | "POST";
  body?: unknown;
  accessToken: string;
};

export type CommercePaymentClient = {
  createPaymentAttempt: (
    accessToken: string,
    checkoutUid: string,
  ) => Promise<PaymentAttemptResponse>;
  getPaymentAttempt: (
    accessToken: string,
    attemptUid: string,
  ) => Promise<PaymentAttemptResponse>;
  initializePaymentAttempt: (
    accessToken: string,
    attemptUid: string,
  ) => Promise<PaymentInitializeResponse>;
};

const buildPaymentUrl = (apiBaseUrl: string, path: string): string => {
  const normalizedBase = apiBaseUrl.replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;

  return `${normalizedBase}/${normalizedPath}`;
};

export const createCommercePaymentClient = ({
  apiBaseUrl,
  lang,
  tenantHeaders,
}: CommercePaymentClientConfig): CommercePaymentClient => {
  const requestPayment = async <T>(
    path: string,
    options: PaymentRequestOptions,
  ): Promise<T> => {
    const headers = new Headers({
      Accept: "application/json",
      "Accept-Language": lang,
      Authorization: `Bearer ${options.accessToken}`,
      ...tenantHeaders,
    });

    if (options.body !== undefined) {
      headers.set("Content-Type", "application/json");
    }

    const response = await fetch(buildPaymentUrl(apiBaseUrl, path), {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
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
    createPaymentAttempt: (accessToken, checkoutUid) =>
      requestPayment<PaymentAttemptResponse>(
        resolveCommerceEndpoint("paymentAttempts"),
        {
          method: "POST",
          body: { checkoutUid } satisfies CreatePaymentAttemptRequest,
          accessToken,
        },
      ),
    getPaymentAttempt: (accessToken, attemptUid) =>
      requestPayment<PaymentAttemptResponse>(
        resolveCommerceEndpoint("paymentAttemptByUid", { attemptUid }),
        { accessToken },
      ),
    initializePaymentAttempt: (accessToken, attemptUid) =>
      requestPayment<PaymentInitializeResponse>(
        resolveCommerceEndpoint("paymentInitialize", { attemptUid }),
        {
          method: "POST",
          accessToken,
        },
      ),
  };
};
