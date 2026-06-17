import type { ApiResponse } from "@/lib/core/http/api-response";
import { resolveCommerceEndpoint } from "@/lib/core/http/endpoints";
import type { CartMutationResult, CartResponse } from "./types";

const CART_TOKEN_HEADER = "X-Cart-Token";

export type CommerceCartClientConfig = {
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
  getCartToken: () => string | null;
  setCartToken: (cartToken: string) => void;
  clearCartToken: () => void;
};

type CartRequestOptions = {
  method?: "GET" | "POST" | "PATCH" | "DELETE";
  body?: unknown;
  allowMissingToken?: boolean;
};

export type CommerceCartClient = {
  createCart: () => Promise<CartMutationResult>;
  getCart: () => Promise<CartResponse | null>;
  addCartItem: (variantUid: string, quantity: number) => Promise<CartMutationResult>;
  updateCartItem: (itemUid: string, quantity: number) => Promise<CartMutationResult>;
  deleteCartItem: (itemUid: string) => Promise<CartMutationResult>;
  clearCart: () => Promise<void>;
};

const buildCartUrl = (apiBaseUrl: string, path: string): string => {
  const normalizedBase = apiBaseUrl.replace(/\/$/, "");
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;

  return `${normalizedBase}/${normalizedPath}`;
};

const readCartTokenFromResponse = (
  response: Response,
  payload: ApiResponse<CartResponse | null>,
): string | null => {
  const bodyToken = payload.data?.cartToken;
  if (bodyToken) {
    return bodyToken;
  }

  return response.headers.get(CART_TOKEN_HEADER);
};

export const createCommerceCartClient = ({
  apiBaseUrl,
  lang,
  tenantHeaders,
  getCartToken,
  setCartToken,
  clearCartToken,
}: CommerceCartClientConfig): CommerceCartClient => {
  const requestCart = async (
    path: string,
    options: CartRequestOptions = {},
  ): Promise<CartMutationResult> => {
    const cartToken = getCartToken();
    if (!options.allowMissingToken && !cartToken) {
      return null;
    }

    const headers = new Headers({
      Accept: "application/json",
      "Accept-Language": lang,
      ...tenantHeaders,
    });

    if (cartToken) {
      headers.set(CART_TOKEN_HEADER, cartToken);
    }

    if (options.body !== undefined) {
      headers.set("Content-Type", "application/json");
    }

    const response = await fetch(buildCartUrl(apiBaseUrl, path), {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      cache: "no-store",
    });

    if (response.status === 404) {
      clearCartToken();
      return null;
    }

    const payload = (await response.json()) as ApiResponse<CartResponse | null>;

    if (!response.ok || payload.result === "ERROR") {
      throw new Error(payload.message ?? "");
    }

    const responseToken = readCartTokenFromResponse(response, payload);
    if (responseToken) {
      setCartToken(responseToken);
    }

    return payload.data ?? null;
  };

  return {
    createCart: () =>
      requestCart(resolveCommerceEndpoint("cart"), {
        method: "POST",
        allowMissingToken: true,
      }),
    getCart: () => requestCart(resolveCommerceEndpoint("cart")),
    addCartItem: (variantUid, quantity) =>
      requestCart(resolveCommerceEndpoint("cartItems"), {
        method: "POST",
        body: { variantUid, quantity },
        allowMissingToken: true,
      }),
    updateCartItem: (itemUid, quantity) =>
      requestCart(resolveCommerceEndpoint("cartItemByUid", { itemUid }), {
        method: "PATCH",
        body: { quantity },
      }),
    deleteCartItem: (itemUid) =>
      requestCart(resolveCommerceEndpoint("cartItemByUid", { itemUid }), {
        method: "DELETE",
      }),
    clearCart: async () => {
      await requestCart(resolveCommerceEndpoint("cart"), { method: "DELETE" });
      clearCartToken();
    },
  };
};
