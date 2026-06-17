export const CART_TOKEN_STORAGE_KEY = "commerce.cartToken:v1";

const canUseStorage = (): boolean => typeof window !== "undefined";

export const getStoredCartToken = (): string | null => {
  if (!canUseStorage()) {
    return null;
  }

  try {
    return window.localStorage.getItem(CART_TOKEN_STORAGE_KEY);
  } catch {
    return null;
  }
};

export const storeCartToken = (cartToken: string | null | undefined): void => {
  if (!canUseStorage() || !cartToken) {
    return;
  }

  try {
    window.localStorage.setItem(CART_TOKEN_STORAGE_KEY, cartToken);
  } catch {
    // Storage can be unavailable in private or restricted browser modes.
  }
};

export const clearStoredCartToken = (): void => {
  if (!canUseStorage()) {
    return;
  }

  try {
    window.localStorage.removeItem(CART_TOKEN_STORAGE_KEY);
  } catch {
    // Storage can be unavailable in private or restricted browser modes.
  }
};
