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
};

export type CommerceOrderFulfillmentResponse = {
  carrierName: string | null;
  trackingNumber: string | null;
  trackingUrl: string | null;
  shippedAt: string | null;
  deliveredAt: string | null;
  statusChangedAt: string | null;
};

export type CommerceOrderDetailResponse = CommerceOrderSummaryResponse & {
  items: CheckoutItemResponse[];
  shipping: CheckoutShippingResponse;
  fulfillment: CommerceOrderFulfillmentResponse;
  deliveryAddress: CheckoutAddressSnapshot | null;
  billingAddress: CheckoutAddressSnapshot | null;
  legalSnapshotStatus: string;
  legalSnapshotJson: string | null;
};

export type CommerceOrderLegalSnapshotDocument = {
  templateUid: string;
  type: string;
  version: number;
  title: string;
  contentText: string;
  contentHash: string;
  acceptedAt: string;
};

export type CommerceOrderLegalSnapshot = {
  language: string;
  capturedAt: string;
  documents: CommerceOrderLegalSnapshotDocument[];
};

export type CommerceOrderResolutionRequestType = "CANCELLATION" | "RETURN";

export type CommerceOrderResolutionRequestResponse = {
  requestUid: string;
  orderUid: string;
  orderNumber: string;
  type: CommerceOrderResolutionRequestType;
  status: string;
  reason: string;
  description: string;
  requestedOrderStatus: string;
  refundStatus: string;
  createdAt: string;
  decidedAt: string | null;
  refundedAt: string | null;
};

export type CreateCommerceOrderResolutionRequestPayload = {
  requestType: CommerceOrderResolutionRequestType;
  reason: string;
  description: string;
};
