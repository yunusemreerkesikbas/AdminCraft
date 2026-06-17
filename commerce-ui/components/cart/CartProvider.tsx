"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  clearStoredCartToken,
  getStoredCartToken,
  storeCartToken,
} from "@/lib/commerce/cart/cart-token-store";
import {
  createCommerceCartClient,
  type CommerceCartClient,
} from "@/lib/commerce/cart/cart-client";
import type { CartResponse } from "@/lib/commerce/cart/types";

type CartContextValue = {
  cart: CartResponse | null;
  isLoading: boolean;
  isMutating: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  addCartItem: (variantUid: string, quantity: number) => Promise<boolean>;
  updateQuantity: (itemUid: string, quantity: number) => Promise<void>;
  removeItem: (itemUid: string) => Promise<void>;
  clearCart: () => Promise<void>;
};

type CartProviderProps = {
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
  children: ReactNode;
};

const CartContext = createContext<CartContextValue | null>(null);

export function CartProvider({
  apiBaseUrl,
  lang,
  tenantHeaders,
  children,
}: CartProviderProps) {
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isMutating, setIsMutating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const client = useMemo<CommerceCartClient>(
    () =>
      createCommerceCartClient({
        apiBaseUrl,
        lang,
        tenantHeaders,
        getCartToken: getStoredCartToken,
        setCartToken: storeCartToken,
        clearCartToken: clearStoredCartToken,
      }),
    [apiBaseUrl, lang, tenantHeaders],
  );

  const refresh = useCallback(async () => {
    const cartToken = getStoredCartToken();
    if (!cartToken) {
      setCart(null);
      setError(null);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const nextCart = await client.getCart();
      setCart(nextCart);
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsLoading(false);
    }
  }, [client]);

  const runMutation = useCallback(
    async (mutation: () => Promise<CartResponse | null | void>): Promise<boolean> => {
      setIsMutating(true);
      setError(null);
      try {
        const nextCart = await mutation();
        setCart(nextCart ?? null);
        return true;
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
        return false;
      } finally {
        setIsMutating(false);
      }
    },
    [],
  );

  const addCartItem = useCallback(
    async (variantUid: string, quantity: number) =>
      runMutation(() => client.addCartItem(variantUid, quantity)),
    [client, runMutation],
  );

  const updateQuantity = useCallback(
    async (itemUid: string, quantity: number) => {
      await runMutation(() => client.updateCartItem(itemUid, quantity));
    },
    [client, runMutation],
  );

  const removeItem = useCallback(
    async (itemUid: string) => {
      await runMutation(() => client.deleteCartItem(itemUid));
    },
    [client, runMutation],
  );

  const clearCart = useCallback(async () => {
    await runMutation(async () => {
      await client.clearCart();
      return null;
    });
  }, [client, runMutation]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const value = useMemo<CartContextValue>(
    () => ({
      cart,
      isLoading,
      isMutating,
      error,
      refresh,
      addCartItem,
      updateQuantity,
      removeItem,
      clearCart,
    }),
    [
      cart,
      isLoading,
      isMutating,
      error,
      refresh,
      addCartItem,
      updateQuantity,
      removeItem,
      clearCart,
    ],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export const useCart = (): CartContextValue => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within CartProvider.");
  }

  return context;
};
