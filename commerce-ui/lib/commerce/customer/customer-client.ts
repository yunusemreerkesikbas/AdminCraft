import type { ApiResponse } from "@/lib/core/http/api-response";
import { resolveCommerceEndpoint } from "@/lib/core/http/endpoints";
import { createRequestTimeoutSignal } from "@/lib/core/http/request-timeout";
import type {
  CommerceCustomer,
  CommerceCustomerAuthResponse,
  CommerceCustomerLoginRequest,
  CommerceCustomerRegisterRequest,
} from "./types";

const CART_TOKEN_HEADER = "X-Cart-Token";

export type CommerceCustomerClientConfig = {
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
  getCartToken: () => string | null;
  setCartToken: (cartToken: string) => void;
  clearCartToken: () => void;
};

type CustomerRequestOptions = {
  method?: "GET" | "POST";
  body?: unknown;
  accessToken?: string | null;
  includeCartToken?: boolean;
  ignoreUnauthorized?: boolean;
};

type CustomerRequestResult<T> = {
  data: T | null;
  response: Response;
};

export type CommerceCustomerClient = {
  login: (
    request: CommerceCustomerLoginRequest,
  ) => Promise<CommerceCustomerAuthResponse>;
  register: (
    request: CommerceCustomerRegisterRequest,
  ) => Promise<CommerceCustomerAuthResponse>;
  refresh: () => Promise<CommerceCustomerAuthResponse | null>;
  logout: () => Promise<void>;
  getProfile: (accessToken: string) => Promise<CommerceCustomer>;
};

const buildCustomerUrl = (apiBaseUrl: string, path: string): string => {
  const normalizedBase = apiBaseUrl.replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;

  return `${normalizedBase}/${normalizedPath}`;
};

const syncCartToken = (
  response: Response | null,
  authResponse: CommerceCustomerAuthResponse | null | undefined,
  setCartToken: (cartToken: string) => void,
  clearCartToken: () => void,
): void => {
  const headerToken = response?.headers.get(CART_TOKEN_HEADER);
  const bodyToken = authResponse?.cart?.cartToken;

  if (bodyToken) {
    setCartToken(bodyToken);
    return;
  }

  if (headerToken) {
    setCartToken(headerToken);
    return;
  }

  if (authResponse?.cart) {
    clearCartToken();
  }
};

export const createCommerceCustomerClient = ({
  apiBaseUrl,
  lang,
  tenantHeaders,
  getCartToken,
  setCartToken,
  clearCartToken,
}: CommerceCustomerClientConfig): CommerceCustomerClient => {
  const request = async <T>(
    path: string,
    options: CustomerRequestOptions = {},
  ): Promise<CustomerRequestResult<T> | null> => {
    const headers = new Headers({
      Accept: "application/json",
      "Accept-Language": lang,
      ...tenantHeaders,
    });

    const cartToken = getCartToken();
    if (options.includeCartToken && cartToken) {
      headers.set(CART_TOKEN_HEADER, cartToken);
    }

    if (options.accessToken) {
      headers.set("Authorization", `Bearer ${options.accessToken}`);
    }

    if (options.body !== undefined) {
      headers.set("Content-Type", "application/json");
    }

    const response = await fetch(buildCustomerUrl(apiBaseUrl, path), {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      cache: "no-store",
      credentials: "include",
      signal: createRequestTimeoutSignal(),
    });

    if (
      options.ignoreUnauthorized &&
      (response.status === 401 || response.status === 403)
    ) {
      return null;
    }

    const payload = (await response.json()) as ApiResponse<T>;

    if (!response.ok || payload.result === "ERROR") {
      throw new Error(payload.message ?? "");
    }

    return {
      data: payload.data ?? null,
      response,
    };
  };

  const requestAuth = async (
    path: string,
    body?: unknown,
    ignoreUnauthorized = false,
  ): Promise<CommerceCustomerAuthResponse | null> => {
    const result = await request<CommerceCustomerAuthResponse>(path, {
      method: "POST",
      body,
      includeCartToken: true,
      ignoreUnauthorized,
    });

    syncCartToken(result?.response ?? null, result?.data, setCartToken, clearCartToken);

    return result?.data ?? null;
  };

  return {
    login: async (loginRequest) => {
      const response = await requestAuth(
        resolveCommerceEndpoint("customerAuthLogin"),
        loginRequest,
      );
      if (!response) {
        throw new Error("");
      }
      return response;
    },
    register: async (registerRequest) => {
      const response = await requestAuth(
        resolveCommerceEndpoint("customerAuthRegister"),
        registerRequest,
      );
      if (!response) {
        throw new Error("");
      }
      return response;
    },
    refresh: async () => {
      const response = await requestAuth(
        resolveCommerceEndpoint("customerAuthRefresh"),
        undefined,
        true,
      );
      return response;
    },
    logout: async () => {
      await request<void>(resolveCommerceEndpoint("customerAuthLogout"), {
        method: "POST",
        ignoreUnauthorized: true,
      });
    },
    getProfile: async (accessToken) => {
      const result = await request<CommerceCustomer>(
        resolveCommerceEndpoint("customerProfile"),
        { accessToken },
      );
      if (!result?.data) {
        throw new Error("");
      }
      return result.data;
    },
  };
};
