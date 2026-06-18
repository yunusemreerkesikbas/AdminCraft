"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { useCart } from "@/components/cart/CartProvider";
import {
  clearStoredCartToken,
  getStoredCartToken,
  storeCartToken,
} from "@/lib/commerce/cart/cart-token-store";
import { createCommerceCustomerClient } from "@/lib/commerce/customer/customer-client";
import {
  clearCustomerAccessToken,
  setCustomerAccessToken,
} from "@/lib/commerce/customer/customer-token-memory";
import type {
  CommerceCustomer,
  CommerceCustomerAddress,
  CommerceCustomerAddressRequest,
  CommerceCustomerAuthResponse,
  CommerceCustomerLoginRequest,
  CommerceCustomerRegisterRequest,
} from "@/lib/commerce/customer/types";

type CustomerSessionContextValue = {
  customer: CommerceCustomer | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isRestoring: boolean;
  isMutating: boolean;
  error: string | null;
  login: (request: CommerceCustomerLoginRequest) => Promise<boolean>;
  register: (request: CommerceCustomerRegisterRequest) => Promise<boolean>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<boolean>;
  listAddresses: () => Promise<CommerceCustomerAddress[]>;
  createAddress: (
    request: CommerceCustomerAddressRequest,
  ) => Promise<CommerceCustomerAddress | null>;
  updateAddress: (
    addressUid: string,
    request: CommerceCustomerAddressRequest,
  ) => Promise<CommerceCustomerAddress | null>;
  deleteAddress: (addressUid: string) => Promise<boolean>;
  setDefaultDelivery: (
    addressUid: string,
  ) => Promise<CommerceCustomerAddress | null>;
  setDefaultBilling: (
    addressUid: string,
  ) => Promise<CommerceCustomerAddress | null>;
};

type CustomerSessionProviderProps = {
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
  children: ReactNode;
};

const CustomerSessionContext =
  createContext<CustomerSessionContextValue | null>(null);

const MAX_ERROR_MESSAGE_LENGTH = 500;

const normalizeSessionError = (error: unknown): string => {
  if (!(error instanceof Error)) {
    return "";
  }

  return error.message.trim().slice(0, MAX_ERROR_MESSAGE_LENGTH);
};

export function CustomerSessionProvider({
  apiBaseUrl,
  lang,
  tenantHeaders,
  children,
}: CustomerSessionProviderProps) {
  const { refresh: refreshCart, replaceCart } = useCart();
  const [customer, setCustomer] = useState<CommerceCustomer | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isRestoring, setIsRestoring] = useState(true);
  const [isMutating, setIsMutating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const didRestore = useRef(false);

  const client = useMemo(
    () =>
      createCommerceCustomerClient({
        apiBaseUrl,
        lang,
        tenantHeaders,
        getCartToken: getStoredCartToken,
        setCartToken: storeCartToken,
        clearCartToken: clearStoredCartToken,
      }),
    [apiBaseUrl, lang, tenantHeaders],
  );

  const applyAuthResponse = useCallback(
    async (response: CommerceCustomerAuthResponse) => {
      setAccessToken(response.accessToken);
      setCustomerAccessToken(response.accessToken);
      setCustomer(response.customer);

      if (response.cart) {
        replaceCart(response.cart);
        return;
      }

      await refreshCart();
    },
    [refreshCart, replaceCart],
  );

  const clearSession = useCallback(() => {
    setAccessToken(null);
    clearCustomerAccessToken();
    setCustomer(null);
  }, []);

  const refreshSession = useCallback(async (): Promise<boolean> => {
    setError(null);
    try {
      const response = await client.refresh();
      if (!response) {
        clearSession();
        return false;
      }

      await applyAuthResponse(response);
      return true;
    } catch {
      clearSession();
      return false;
    }
  }, [applyAuthResponse, clearSession, client]);

  const runAuthMutation = useCallback(
    async (
      mutation: () => Promise<CommerceCustomerAuthResponse>,
    ): Promise<boolean> => {
      setIsMutating(true);
      setError(null);
      try {
        const response = await mutation();
        await applyAuthResponse(response);
        return true;
      } catch (err) {
        clearSession();
        setError(normalizeSessionError(err));
        return false;
      } finally {
        setIsMutating(false);
      }
    },
    [applyAuthResponse, clearSession],
  );

  const login = useCallback(
    (request: CommerceCustomerLoginRequest) =>
      runAuthMutation(() => client.login(request)),
    [client, runAuthMutation],
  );

  const register = useCallback(
    (request: CommerceCustomerRegisterRequest) =>
      runAuthMutation(() => client.register(request)),
    [client, runAuthMutation],
  );

  const logout = useCallback(async () => {
    setIsMutating(true);
    setError(null);
    try {
      await client.logout();
    } catch (err) {
      setError(normalizeSessionError(err));
    } finally {
      clearSession();
      replaceCart(null);
      setIsMutating(false);
    }
  }, [clearSession, client, replaceCart]);

  const requireAccessToken = useCallback((): string => {
    const currentAccessToken = accessToken;
    if (!currentAccessToken) {
      throw new Error("");
    }
    return currentAccessToken;
  }, [accessToken]);

  const listAddresses = useCallback(
    () => client.listAddresses(requireAccessToken()),
    [client, requireAccessToken],
  );

  const createAddress = useCallback(
    async (request: CommerceCustomerAddressRequest) => {
      setIsMutating(true);
      setError(null);
      try {
        return await client.createAddress(requireAccessToken(), request);
      } catch (err) {
        setError(normalizeSessionError(err));
        return null;
      } finally {
        setIsMutating(false);
      }
    },
    [client, requireAccessToken],
  );

  const updateAddress = useCallback(
    async (addressUid: string, request: CommerceCustomerAddressRequest) => {
      setIsMutating(true);
      setError(null);
      try {
        return await client.updateAddress(
          requireAccessToken(),
          addressUid,
          request,
        );
      } catch (err) {
        setError(normalizeSessionError(err));
        return null;
      } finally {
        setIsMutating(false);
      }
    },
    [client, requireAccessToken],
  );

  const deleteAddress = useCallback(
    async (addressUid: string) => {
      setIsMutating(true);
      setError(null);
      try {
        await client.deleteAddress(requireAccessToken(), addressUid);
        return true;
      } catch (err) {
        setError(normalizeSessionError(err));
        return false;
      } finally {
        setIsMutating(false);
      }
    },
    [client, requireAccessToken],
  );

  const setDefaultDelivery = useCallback(
    async (addressUid: string) => {
      setIsMutating(true);
      setError(null);
      try {
        return await client.setDefaultDelivery(requireAccessToken(), addressUid);
      } catch (err) {
        setError(normalizeSessionError(err));
        return null;
      } finally {
        setIsMutating(false);
      }
    },
    [client, requireAccessToken],
  );

  const setDefaultBilling = useCallback(
    async (addressUid: string) => {
      setIsMutating(true);
      setError(null);
      try {
        return await client.setDefaultBilling(requireAccessToken(), addressUid);
      } catch (err) {
        setError(normalizeSessionError(err));
        return null;
      } finally {
        setIsMutating(false);
      }
    },
    [client, requireAccessToken],
  );

  useEffect(() => {
    if (didRestore.current) {
      return;
    }

    didRestore.current = true;
    setIsRestoring(true);
    void refreshSession().finally(() => {
      setIsRestoring(false);
    });
  }, [refreshSession]);

  const value = useMemo<CustomerSessionContextValue>(
    () => ({
      customer,
      accessToken,
      isAuthenticated: Boolean(accessToken && customer),
      isRestoring,
      isMutating,
      error,
      login,
      register,
      logout,
      refreshSession,
      listAddresses,
      createAddress,
      updateAddress,
      deleteAddress,
      setDefaultDelivery,
      setDefaultBilling,
    }),
    [
      customer,
      accessToken,
      isRestoring,
      isMutating,
      error,
      login,
      register,
      logout,
      refreshSession,
      listAddresses,
      createAddress,
      updateAddress,
      deleteAddress,
      setDefaultDelivery,
      setDefaultBilling,
    ],
  );

  return (
    <CustomerSessionContext.Provider value={value}>
      {children}
    </CustomerSessionContext.Provider>
  );
}

export const useCustomerSession = (): CustomerSessionContextValue => {
  const context = useContext(CustomerSessionContext);
  if (!context) {
    throw new Error(
      "useCustomerSession must be used within CustomerSessionProvider.",
    );
  }

  return context;
};
