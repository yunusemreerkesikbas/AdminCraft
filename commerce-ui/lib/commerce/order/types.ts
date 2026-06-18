import type {
  CheckoutAddressSnapshot,
  CheckoutItemResponse,
  CheckoutShippingResponse,
  CheckoutTotalsResponse,
} from "@/lib/commerce/checkout/types";

export type PageableResponse<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  sortConfig: {
    currentSort: {
      field: string;
      direction: string;
      code: string;
    } | null;
    availableSorts: Array<{
      code: string;
      labelKey: string;
      isDefault: boolean;
    }>;
  } | null;
};

export type CommerceOrderSummaryResponse = {
  orderUid: string;
  orderNumber: string;
  status: string;
  createdAt: string;
  currencyIso: string;
  totals: CheckoutTotalsResponse;
  itemCount: number;
  requiresAttention: boolean | null;
  attentionReasonKey: string | null;
};

export type CommerceOrderDetailResponse = CommerceOrderSummaryResponse & {
  items: CheckoutItemResponse[];
  shipping: CheckoutShippingResponse;
  deliveryAddress: CheckoutAddressSnapshot | null;
  billingAddress: CheckoutAddressSnapshot | null;
  legalSnapshotStatus: string;
};
