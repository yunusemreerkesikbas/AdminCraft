import type { CheckoutTotalsResponse } from "@/lib/commerce/checkout/types";

export type PaymentAttemptStatus =
  | "PENDING"
  | "INITIALIZING"
  | "SUCCEEDED"
  | "FAILED"
  | "EXPIRED";

export type PaymentAttemptStatusValue = PaymentAttemptStatus | (string & {});

export type PaymentAttemptResponse = {
  attemptUid: string;
  checkoutUid: string;
  status: PaymentAttemptStatusValue;
  provider: string;
  currencyIso: string;
  totals: CheckoutTotalsResponse;
  expiresAt: string;
  failureMessageKey: string | null;
};

export type PaymentInitializeResponse = {
  attemptUid: string;
  status: PaymentAttemptStatusValue;
  provider: string;
  paymentPageUrl: string;
};

export type LegalAcceptanceRequest = {
  templateUid: string;
  version: number;
  accepted: boolean;
};

export type CreatePaymentAttemptRequest = {
  checkoutUid: string;
  legalAcceptances: LegalAcceptanceRequest[];
};
