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

export type InvoiceType = "INDIVIDUAL" | "CORPORATE";

export type CommerceCustomerAddress = {
  uid: string;
  label: string | null;
  firstName: string;
  lastName: string;
  phone: string;
  countryIso: string;
  city: string;
  district: string;
  addressLine1: string;
  addressLine2: string | null;
  postalCode: string | null;
  defaultDelivery: boolean;
  defaultBilling: boolean;
  invoiceType: InvoiceType;
  companyName: string | null;
  taxNumber: string | null;
  taxOffice: string | null;
  invoiceIdentityNumber: string | null;
};

export type CommerceCustomerAddressRequest = {
  label?: string | null;
  firstName: string;
  lastName: string;
  phone: string;
  countryIso?: string | null;
  city: string;
  district: string;
  addressLine1: string;
  addressLine2?: string | null;
  postalCode?: string | null;
  defaultDelivery?: boolean;
  defaultBilling?: boolean;
  invoiceType?: InvoiceType;
  companyName?: string | null;
  taxNumber?: string | null;
  taxOffice?: string | null;
  invoiceIdentityNumber?: string | null;
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
