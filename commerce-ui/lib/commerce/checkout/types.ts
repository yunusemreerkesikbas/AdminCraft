export type CheckoutAddressSelectionRequest = {
  deliveryAddressUid?: string | null;
  billingAddressUid?: string | null;
  billingSameAsDelivery?: boolean | null;
};

export type CheckoutAddressSnapshot = {
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
  invoiceType: string;
  companyName: string | null;
  taxNumber: string | null;
  taxOffice: string | null;
  invoiceIdentityNumber: string | null;
};

export type CheckoutItemResponse = {
  uid: string;
  productUid: string;
  productSku: string | null;
  variantUid: string;
  variantSku: string | null;
  quantity: number;
  unitGrossPrice: number | string;
  vatRate: number | string;
  lineTotal: number | string;
  lineVatTotal: number | string;
};

export type CheckoutTotalsResponse = {
  currencyIso: string;
  subtotal: number | string;
  vatTotal: number | string;
  shippingTotal: number | string;
  total: number | string;
};

export type CheckoutShippingResponse = {
  methodCode: string | null;
  methodNameKey: string | null;
  amount: number | string;
};

export type CheckoutValidationResponse = {
  valid: boolean;
  cartChanged: boolean;
  priceChanged: boolean;
  stockChanged: boolean;
  warningMessageKeys: string[];
};

export type CheckoutResponse = {
  checkoutUid: string;
  status: string;
  expiresAt: string;
  deliveryAddress: CheckoutAddressSnapshot | null;
  billingAddress: CheckoutAddressSnapshot | null;
  items: CheckoutItemResponse[];
  totals: CheckoutTotalsResponse;
  shipping: CheckoutShippingResponse;
  validation: CheckoutValidationResponse;
};
