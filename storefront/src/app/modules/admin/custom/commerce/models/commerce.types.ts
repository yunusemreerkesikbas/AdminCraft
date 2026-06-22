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
    lowStockVariantCount: number;
    failedPaymentAttemptCount: number;
    failedNotificationCount: number;
    currencyIso: string;
}

export interface CommerceAdminOrderRow {
    id: number;
    orderUid: string;
    orderNumber: string;
    customerUid: string;
    customerName: string | null;
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

export interface CommerceOrderFulfillment {
    carrierName?: string | null;
    trackingNumber?: string | null;
    trackingUrl?: string | null;
    shippedAt?: string | null;
    deliveredAt?: string | null;
    statusChangedAt?: string | null;
}

export interface CommerceOrderStatusHistory {
    uid: string;
    fromStatus: string;
    toStatus: string;
    carrierName?: string | null;
    trackingNumber?: string | null;
    trackingUrl?: string | null;
    internalNote?: string | null;
    changedByUserId?: number | null;
    changedByEmail?: string | null;
    createdAt: string;
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
    legalSnapshotJson?: string | null;
    stockDeducted: boolean;
    items: CommerceOrderItem[];
    shipping: CommerceShipping;
    fulfillment: CommerceOrderFulfillment;
    deliveryAddress: CommerceAddressSnapshot;
    billingAddress: CommerceAddressSnapshot;
    paymentAttempt: CommerceAdminOrderPayment;
    statusHistory: CommerceOrderStatusHistory[];
}

export interface ChangeCommerceOrderStatusRequest {
    status: 'PREPARING' | 'SHIPPED' | 'DELIVERED';
    carrierName?: string | null;
    trackingNumber?: string | null;
    trackingUrl?: string | null;
    internalNote?: string | null;
}

export interface CommerceAdminPaymentAttemptRow {
    id: number;
    attemptUid: string;
    checkoutUid: string;
    customerUid: string;
    customerName: string | null;
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

export interface CommerceOrderResolutionRequestRow {
    id: number;
    requestUid: string;
    orderUid: string;
    orderNumber: string;
    customerEmail: string;
    type: string;
    status: string;
    reason: string;
    description: string;
    previousOrderStatus: string;
    requestedOrderStatus: string;
    decisionNote?: string | null;
    decidedByUserId?: number | null;
    decidedByEmail?: string | null;
    refundStatus: string;
    refundProvider?: string | null;
    refundReference?: string | null;
    refundFailureCode?: string | null;
    refundFailureMessageKey?: string | null;
    stockRestored: boolean;
    orderTotal: number;
    currencyIso: string;
    createdAt: string;
    decidedAt?: string | null;
    refundAttemptedAt?: string | null;
    refundedAt?: string | null;
}

export interface CommerceOrderResolutionDecisionRequest {
    decision: 'APPROVE' | 'REJECT';
    decisionNote?: string | null;
}

export type CommerceLegalTemplateType =
    | 'DISTANCE_SALES_AGREEMENT'
    | 'PRE_INFORMATION_FORM';

export type CommerceLegalTemplateStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface CommerceLegalTemplate {
    templateUid: string;
    type: CommerceLegalTemplateType;
    language: string;
    version: number;
    status: CommerceLegalTemplateStatus;
    title: string;
    contentText: string;
    publishedAt?: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CommerceLegalTemplateRequest {
    type: CommerceLegalTemplateType;
    language: string;
    title: string;
    contentText: string;
}

export interface CommerceLegalTemplatePreview {
    templateUid: string;
    content: string;
}

export interface CommerceLegalSnapshotDocument {
    templateUid: string;
    type: CommerceLegalTemplateType;
    language: string;
    version: number;
    title: string;
    content: string;
    contentHash: string;
}

export interface CommerceLegalSnapshot {
    language: string;
    capturedAt: string;
    acceptedAt: string;
    documents: CommerceLegalSnapshotDocument[];
}

export interface CommerceNotificationOutboxRow {
    id: number;
    outboxUid: string;
    eventType: string;
    channel: string;
    aggregateType: string;
    aggregateUid: string;
    recipientEmail?: string | null;
    recipientPhone?: string | null;
    language: string;
    subject: string;
    content: string;
    status: string;
    attemptCount: number;
    maxRetryAttempts: number;
    retryAllowed: boolean;
    providerMessageId?: string | null;
    errorMessage?: string | null;
    lastAttemptedAt?: string | null;
    nextRetryAt?: string | null;
    sentAt?: string | null;
    createdAt: string;
    updatedAt: string;
}

export type CommerceNotificationChannel = 'EMAIL' | 'SMS';

export interface CommerceNotificationTemplate {
    templateUid: string;
    eventType: string;
    channel: CommerceNotificationChannel;
    language: string;
    subject: string;
    content: string;
    active: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CommerceNotificationTemplateRequest {
    subject: string;
    content: string;
    active: boolean;
}

export interface CommerceNotificationTemplatePreview {
    templateUid: string;
    subject: string;
    content: string;
}
