export const COMMERCE_MODULE_CODE = 'commerce' as const;

export type CommerceModuleCode = typeof COMMERCE_MODULE_CODE;

export interface CommerceTotals {
    currencyIso: string;
    subtotal: number;
    vatTotal: number;
    shippingTotal: number;
    total: number;
}

export interface CommerceAdminMetric {
    orderCount: number;
    revenue: number;
    currencyIso: string;
}

export interface CommerceAdminDashboard {
    today: CommerceAdminMetric;
    lastSevenDays: CommerceAdminMetric;
    attentionOrderCount: number;
    failedPaymentAttemptCount: number;
    currencyIso: string;
}

export interface CommerceAdminOrderRow {
    id: number;
    orderUid: string;
    orderNumber: string;
    customerUid: string;
    customerName: string;
    customerEmail: string;
    status: string;
    createdAt: string;
    currencyIso: string;
    totals: CommerceTotals;
    itemCount: number;
    provider: string;
    requiresAttention: boolean;
    attentionReasonKey?: string | null;
}

export interface CommerceOrderItem {
    uid: string;
    productUid: string;
    productSku: string;
    variantUid: string;
    variantSku: string;
    quantity: number;
    unitGrossPrice: number;
    vatRate: number;
    lineTotal: number;
    lineVatTotal: number;
}

export interface CommerceAddressSnapshot {
    uid: string;
    label: string;
    firstName: string;
    lastName: string;
    phone: string;
    countryIso: string;
    city: string;
    district: string;
    addressLine1: string;
    addressLine2?: string | null;
    postalCode?: string | null;
    invoiceType: string;
    companyName?: string | null;
    taxNumber?: string | null;
    taxOffice?: string | null;
    invoiceIdentityNumber?: string | null;
}

export interface CommerceShipping {
    methodCode: string;
    methodName: string;
    total: number;
}

export interface CommerceAdminOrderPayment {
    attemptUid: string;
    status: string;
    provider: string;
    providerReference?: string | null;
    providerTransactionId?: string | null;
    failureCode?: string | null;
    failureMessageKey?: string | null;
    createdAt: string;
    expiresAt: string;
}

export interface CommerceAdminOrderDetail {
    summary: CommerceAdminOrderRow;
    customerPhone: string;
    providerTransactionId?: string | null;
    legalSnapshotStatus: string;
    stockDeducted: boolean;
    items: CommerceOrderItem[];
    shipping: CommerceShipping;
    deliveryAddress: CommerceAddressSnapshot;
    billingAddress: CommerceAddressSnapshot;
    paymentAttempt: CommerceAdminOrderPayment;
}

export interface CommerceAdminPaymentAttemptRow {
    id: number;
    attemptUid: string;
    checkoutUid: string;
    customerUid: string;
    customerName: string;
    customerEmail: string;
    status: string;
    provider: string;
    currencyIso: string;
    totals: CommerceTotals;
    createdAt: string;
    expiresAt: string;
    providerReference?: string | null;
    providerTransactionId?: string | null;
    failureCode?: string | null;
    failureMessageKey?: string | null;
}
