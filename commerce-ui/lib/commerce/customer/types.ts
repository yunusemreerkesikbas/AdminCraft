import type { CartResponse } from "@/lib/commerce/cart/types";

export type CommerceCustomer = {
  uid: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  gender: string | null;
  birthDate: string | null;
  status: string;
  emailVerified: boolean;
};

export type CartMergeResponse = {
  status: string;
  mergedItemCount: number;
  skippedItemCount: number;
  warningMessageKeys: string[];
};

export type CommerceCustomerAuthResponse = {
  accessToken: string;
  expiresInSeconds: number;
  customer: CommerceCustomer;
  cart: CartResponse | null;
  cartMerge: CartMergeResponse | null;
};

export type CommerceCustomerLoginRequest = {
  email: string;
  password: string;
  rememberMe?: boolean;
  deviceFingerprint?: string;
};

export type CommerceCustomerRegisterRequest = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string;
  termsAccepted: boolean;
  privacyAccepted: boolean;
  rememberMe?: boolean;
  deviceFingerprint?: string;
  source?: string;
};
