export type CartTotalsResponse = {
  currencyIso: string;
  itemCount: number;
  subtotal: number | string;
  vatTotal: number | string;
  total: number | string;
  currentVatTotal: number | string;
  currentTotal: number | string;
};

export type CartItemResponse = {
  itemUid: string;
  productUid: string;
  productSku: string | null;
  variantUid: string;
  variantSku: string | null;
  quantity: number;
  unitPrice: number | string;
  currentUnitPrice: number | string;
  vatRate: number | string;
  lineTotal: number | string;
  priceChanged: boolean;
  available: boolean;
  stockQuantity: number | null;
};

export type CartResponse = {
  cartToken: string | null;
  cartUid: string;
  status: string;
  expiresAt: string;
  items: CartItemResponse[];
  totals: CartTotalsResponse;
};

export type CartMutationResult = CartResponse | null;
