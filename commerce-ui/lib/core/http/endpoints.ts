export const COMMERCE_ENDPOINTS = {
  cart: "commerce/cart",
  cartItems: "commerce/cart/items",
  cartItemByUid: "commerce/cart/items/${itemUid}",
  checkout: "commerce/checkout",
  checkoutAddresses: "commerce/checkout/${checkoutUid}/addresses",
  currentCheckout: "commerce/checkout/current",
  customerAuthRegister: "commerce/customers/auth/register",
  customerAuthLogin: "commerce/customers/auth/login",
  customerAuthRefresh: "commerce/customers/auth/refresh",
  customerAuthLogout: "commerce/customers/auth/logout",
  customerProfile: "commerce/customers/me",
  customerAddresses: "commerce/customers/addresses",
  paymentAttempts: "commerce/payments/attempts",
  paymentAttemptByUid: "commerce/payments/attempts/${attemptUid}",
  paymentInitialize: "commerce/payments/attempts/${attemptUid}/initialize",
  orders: "commerce/orders",
  orderByUid: "commerce/orders/${orderUid}",
} as const;

export type CommerceEndpointKey = keyof typeof COMMERCE_ENDPOINTS;

export const resolveCommerceEndpoint = (
  endpointKey: CommerceEndpointKey,
  params: Record<string, string | number> = {},
): string => {
  const template = COMMERCE_ENDPOINTS[endpointKey];

  return template.replace(/\$\{(\w+)\}/g, (_match, key) => {
    const value = params[key];

    if (value === undefined || value === null) {
      throw new Error(`Parameter '${key}' is required but not provided`);
    }

    return encodeURIComponent(String(value));
  });
};
