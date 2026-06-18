"use client";

import { useEffect, useMemo, useState } from "react";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  ReceiptFrame,
} from "@/components/ui/StorefrontPrimitives";
import { useCustomerSession } from "@/components/customer/CustomerSessionProvider";
import { createCommercePaymentClient } from "@/lib/commerce/payment/payment-client";
import type { PaymentAttemptResponse } from "@/lib/commerce/payment/types";

export type CheckoutReturnCopy = {
  eyebrow: string;
  title: string;
  successTitle: string;
  failureTitle: string;
  unknownTitle: string;
  successDescription: string;
  failureDescription: string;
  unknownDescription: string;
  loadingLabel: string;
  summaryTitle: string;
  summaryNote: string;
  rowStatus: string;
  rowAttempt: string;
  rowOrder: string;
  rowProvider: string;
  rowTotal: string;
  missingValue: string;
  primaryAction: string;
  orderAction: string;
  retryAction: string;
  secondaryAction: string;
  errorFallback: string;
};

type CheckoutReturnViewProps = {
  copy: CheckoutReturnCopy;
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
  paymentStatus: string | null;
  attemptUid: string | null;
  orderUid: string | null;
  ordersHref: string;
  orderHref: string | null;
  checkoutHref: string;
  storeHref: string;
};

const LAST_PAYMENT_ATTEMPT_KEY = "commerce-ui:last-payment-attempt";

const readStoredAttemptUid = (): string | null => {
  try {
    const raw = window.sessionStorage.getItem(LAST_PAYMENT_ATTEMPT_KEY);
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as { attemptUid?: unknown };
    return typeof parsed.attemptUid === "string" ? parsed.attemptUid : null;
  } catch {
    return null;
  }
};

const toNumber = (value: number | string | null | undefined): number => {
  if (typeof value === "number") {
    return value;
  }
  if (typeof value === "string") {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }
  return 0;
};

const formatMoney = (
  lang: string,
  currencyIso: string | undefined,
  value: number | string | null | undefined,
): string =>
  new Intl.NumberFormat(lang, {
    style: "currency",
    currency: currencyIso || "TRY",
  }).format(toNumber(value));

export function CheckoutReturnView({
  copy,
  apiBaseUrl,
  lang,
  tenantHeaders,
  paymentStatus,
  attemptUid,
  orderUid,
  ordersHref,
  orderHref,
  checkoutHref,
  storeHref,
}: CheckoutReturnViewProps) {
  const [attempt, setAttempt] = useState<PaymentAttemptResponse | null>(null);
  const [storedAttemptUid, setStoredAttemptUid] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const { accessToken, isAuthenticated, isRestoring } = useCustomerSession();
  const paymentClient = useMemo(
    () => createCommercePaymentClient({ apiBaseUrl, lang, tenantHeaders }),
    [apiBaseUrl, lang, tenantHeaders],
  );

  useEffect(() => {
    if (attemptUid) {
      return;
    }
    window.setTimeout(() => {
      setStoredAttemptUid(readStoredAttemptUid());
    }, 0);
  }, [attemptUid]);

  const resolvedAttemptUid = attemptUid ?? storedAttemptUid;
  const shouldVerifyAttempt =
    !isRestoring && Boolean(isAuthenticated && accessToken && resolvedAttemptUid);

  useEffect(() => {
    if (isRestoring || !isAuthenticated || !accessToken || !resolvedAttemptUid) {
      return;
    }

    let cancelled = false;
    paymentClient
      .getPaymentAttempt(accessToken, resolvedAttemptUid)
      .then((nextAttempt) => {
        if (!cancelled) {
          setAttempt(nextAttempt);
          setError(null);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "");
        }
      });

    return () => {
      cancelled = true;
    };
  }, [
    accessToken,
    isAuthenticated,
    isRestoring,
    paymentClient,
    resolvedAttemptUid,
  ]);

  const effectiveStatus = attempt?.status ?? paymentStatus ?? null;
  const isSuccess = effectiveStatus === "SUCCEEDED";
  const isFailure = effectiveStatus === "FAILED" || effectiveStatus === "EXPIRED";
  const title = isSuccess
    ? copy.successTitle
    : isFailure
      ? copy.failureTitle
      : copy.unknownTitle;
  const description = isSuccess
    ? copy.successDescription
    : isFailure
      ? copy.failureDescription
      : copy.unknownDescription;
  const primaryHref = isSuccess ? orderHref || ordersHref : checkoutHref;
  const primaryLabel = isSuccess
    ? orderHref
      ? copy.orderAction
      : copy.primaryAction
    : copy.retryAction;
  const isLoading = shouldVerifyAttempt && !attempt && !error;

  return (
    <PageShell
      eyebrow={copy.eyebrow}
      title={title}
      description={isLoading ? copy.loadingLabel : description}
      actions={
        <>
          <ActionLink href={primaryHref} label={primaryLabel} />
          <ActionLink
            href={isSuccess ? ordersHref : storeHref}
            label={isSuccess ? copy.primaryAction : copy.secondaryAction}
            variant="secondary"
          />
        </>
      }
      visual={
        <ReceiptFrame
          title={copy.summaryTitle}
          note={error ? error || copy.errorFallback : copy.summaryNote}
          rows={[
            {
              label: copy.rowStatus,
              value: effectiveStatus ?? copy.missingValue,
            },
            {
              label: copy.rowAttempt,
              value: attempt?.attemptUid ?? resolvedAttemptUid ?? copy.missingValue,
            },
            {
              label: copy.rowOrder,
              value: orderUid ?? copy.missingValue,
            },
            {
              label: copy.rowProvider,
              value: attempt?.provider ?? copy.missingValue,
            },
          ]}
          totalLabel={copy.rowTotal}
          totalValue={
            attempt
              ? formatMoney(lang, attempt.currencyIso, attempt.totals.total)
              : copy.missingValue
          }
        />
      }
    />
  );
}
