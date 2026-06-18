"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import {
  InlineCustomerAuthGate,
  type InlineCustomerAuthCopy,
} from "@/components/customer/InlineCustomerAuthGate";
import { useCustomerSession } from "@/components/customer/CustomerSessionProvider";
import { PageShell } from "@/components/ui/PageShell";
import { ActionLink, ReceiptFrame } from "@/components/ui/StorefrontPrimitives";
import { createCommerceOrderClient } from "@/lib/commerce/order/order-client";
import type {
  CommerceOrderDetailResponse,
  CommerceOrderSummaryResponse,
} from "@/lib/commerce/order/types";
import { withLocalePath } from "@/lib/core/i18n/locale";

export type OrdersCopy = {
  eyebrow: string;
  title: string;
  description: string;
  detailTitle: string;
  listTitle: string;
  emptyTitle: string;
  emptyDescription: string;
  loadingLabel: string;
  errorFallback: string;
  rowOrderUid: string;
  rowItems: string;
  rowStatus: string;
  rowCreatedAt: string;
  rowShipping: string;
  rowDelivery: string;
  rowBilling: string;
  rowLegal: string;
  rowTotal: string;
  attentionLabel: string;
  detailsAction: string;
  primaryAction: string;
  secondaryAction: string;
  itemFallback: string;
  auth: InlineCustomerAuthCopy;
};

type OrdersViewProps = {
  copy: OrdersCopy;
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
};

type OrderDetailViewProps = OrdersViewProps & {
  orderUid: string;
};

type LoadState = {
  key: string | null;
  status: "idle" | "loaded" | "error";
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

const formatDate = (lang: string, value: string): string =>
  new Intl.DateTimeFormat(lang, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));

const addressSummary = (
  address: CommerceOrderDetailResponse["deliveryAddress"],
  fallback: string,
): string => {
  if (!address) {
    return fallback;
  }
  return `${address.firstName} ${address.lastName} - ${address.district}, ${address.city}`;
};

export function OrdersView({
  copy,
  apiBaseUrl,
  lang,
  tenantHeaders,
}: OrdersViewProps) {
  const { accessToken, isAuthenticated, isRestoring } = useCustomerSession();
  const [orders, setOrders] = useState<CommerceOrderSummaryResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loadState, setLoadState] = useState<LoadState>({
    key: null,
    status: "idle",
  });
  const orderClient = useMemo(
    () => createCommerceOrderClient({ apiBaseUrl, lang, tenantHeaders }),
    [apiBaseUrl, lang, tenantHeaders],
  );

  useEffect(() => {
    if (isRestoring || !isAuthenticated || !accessToken) {
      return;
    }

    let cancelled = false;
    orderClient
      .listOrders(accessToken)
      .then((page) => {
        if (!cancelled) {
          setOrders(page.content);
          setError(null);
          setLoadState({ key: accessToken, status: "loaded" });
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "");
          setLoadState({ key: accessToken, status: "error" });
        }
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken, isAuthenticated, isRestoring, orderClient]);
  const currentError = loadState.key === accessToken ? error : null;
  const visibleOrders = loadState.key === accessToken ? orders : [];
  const isLoading =
    !isRestoring &&
    Boolean(isAuthenticated && accessToken) &&
    loadState.key !== accessToken;

  return (
    <PageShell
      eyebrow={copy.eyebrow}
      title={copy.title}
      description={copy.description}
      visual={
        <ReceiptFrame
          title={copy.listTitle}
          note={
            isLoading ? copy.loadingLabel : currentError || copy.emptyDescription
          }
          rows={[
            { label: copy.rowItems, value: String(orders.length) },
            {
              label: copy.rowTotal,
              value:
                visibleOrders.length > 0
                  ? formatMoney(
                      lang,
                      visibleOrders[0].currencyIso,
                      visibleOrders[0].totals.total,
                    )
                  : "-",
            },
          ]}
        />
      }
    >
      {isRestoring ? (
        <section className="surface-panel account-loading">
          <p className="row-title">{copy.loadingLabel}</p>
        </section>
      ) : !isAuthenticated ? (
        <InlineCustomerAuthGate copy={copy.auth} source="commerce-ui-orders" />
      ) : (
        <section className="orders-panel">
          {currentError ? (
            <p className="account-form-error" role="alert">
              {currentError || copy.errorFallback}
            </p>
          ) : null}
          {visibleOrders.length === 0 && !isLoading ? (
            <article className="surface-panel order-empty">
              <h2 className="frame-title">{copy.emptyTitle}</h2>
              <p className="frame-note">{copy.emptyDescription}</p>
            </article>
          ) : null}
          {visibleOrders.map((order) => (
            <article key={order.orderUid} className="surface-panel order-card">
              <div>
                <span className="quiet-chip">{order.status}</span>
                {order.requiresAttention ? (
                  <span className="quiet-chip quiet-chip--attention">
                    {copy.attentionLabel}
                  </span>
                ) : null}
              </div>
              <div>
                <h2 className="frame-title">{order.orderNumber}</h2>
                <p className="frame-note">{formatDate(lang, order.createdAt)}</p>
              </div>
              <dl className="order-meta">
                <div>
                  <dt>{copy.rowItems}</dt>
                  <dd>{order.itemCount}</dd>
                </div>
                <div>
                  <dt>{copy.rowTotal}</dt>
                  <dd>
                    {formatMoney(lang, order.currencyIso, order.totals.total)}
                  </dd>
                </div>
              </dl>
              <Link
                className="commerce-action commerce-action--secondary"
                href={withLocalePath(lang, `account/orders/${order.orderUid}`)}
              >
                {copy.detailsAction}
              </Link>
            </article>
          ))}
        </section>
      )}
    </PageShell>
  );
}

export function OrderDetailView({
  copy,
  apiBaseUrl,
  lang,
  tenantHeaders,
  orderUid,
}: OrderDetailViewProps) {
  const { accessToken, isAuthenticated, isRestoring } = useCustomerSession();
  const [order, setOrder] = useState<CommerceOrderDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loadState, setLoadState] = useState<LoadState>({
    key: null,
    status: "idle",
  });
  const orderClient = useMemo(
    () => createCommerceOrderClient({ apiBaseUrl, lang, tenantHeaders }),
    [apiBaseUrl, lang, tenantHeaders],
  );

  useEffect(() => {
    if (isRestoring || !isAuthenticated || !accessToken) {
      return;
    }

    const requestKey = `${accessToken}:${orderUid}`;
    let cancelled = false;
    orderClient
      .getOrder(accessToken, orderUid)
      .then((nextOrder) => {
        if (!cancelled) {
          setOrder(nextOrder);
          setError(null);
          setLoadState({ key: requestKey, status: "loaded" });
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "");
          setLoadState({ key: requestKey, status: "error" });
        }
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken, isAuthenticated, isRestoring, orderClient, orderUid]);
  const requestKey = accessToken ? `${accessToken}:${orderUid}` : null;
  const currentError = loadState.key === requestKey ? error : null;
  const isLoading =
    !isRestoring &&
    Boolean(isAuthenticated && accessToken) &&
    loadState.key !== requestKey;

  return (
    <PageShell
      eyebrow={copy.eyebrow}
      title={order?.orderNumber ?? copy.detailTitle}
      description={isLoading ? copy.loadingLabel : copy.description}
      actions={
        <ActionLink
          href={withLocalePath(lang, "account/orders")}
          label={copy.primaryAction}
          variant="secondary"
        />
      }
      visual={
        <ReceiptFrame
          title={copy.detailTitle}
          note={currentError ? currentError || copy.errorFallback : undefined}
          rows={[
            { label: copy.rowOrderUid, value: order?.orderUid ?? orderUid },
            { label: copy.rowStatus, value: order?.status ?? "-" },
            {
              label: copy.rowCreatedAt,
              value: order ? formatDate(lang, order.createdAt) : "-",
            },
            { label: copy.rowItems, value: String(order?.itemCount ?? 0) },
            { label: copy.rowLegal, value: order?.legalSnapshotStatus ?? "-" },
          ]}
          totalLabel={copy.rowTotal}
          totalValue={
            order ? formatMoney(lang, order.currencyIso, order.totals.total) : "-"
          }
        />
      }
    >
      {isRestoring ? (
        <section className="surface-panel account-loading">
          <p className="row-title">{copy.loadingLabel}</p>
        </section>
      ) : !isAuthenticated ? (
        <InlineCustomerAuthGate
          copy={copy.auth}
          source="commerce-ui-order-detail"
        />
      ) : (
        <section className="orders-panel">
          {currentError ? (
            <p className="account-form-error" role="alert">
              {currentError || copy.errorFallback}
            </p>
          ) : null}
          {order ? (
            <>
              <section className="surface-panel order-detail-panel">
                <h2 className="frame-title">{copy.rowItems}</h2>
                <div className="checkout-items">
                  {order.items.map((item) => (
                    <article key={item.uid} className="checkout-item-row">
                      <span>
                        {item.productSku ||
                          item.variantSku ||
                          item.productUid ||
                          copy.itemFallback}
                      </span>
                      <strong>
                        {item.quantity} x{" "}
                        {formatMoney(
                          lang,
                          order.currencyIso,
                          item.unitGrossPrice,
                        )}
                      </strong>
                    </article>
                  ))}
                </div>
              </section>
              <section className="surface-panel order-detail-panel">
                <h2 className="frame-title">{copy.rowShipping}</h2>
                <dl className="order-meta order-meta--stacked">
                  <div>
                    <dt>{copy.rowShipping}</dt>
                    <dd>{order.shipping.methodNameKey}</dd>
                  </div>
                  <div>
                    <dt>{copy.rowDelivery}</dt>
                    <dd>
                      {addressSummary(order.deliveryAddress, copy.emptyDescription)}
                    </dd>
                  </div>
                  <div>
                    <dt>{copy.rowBilling}</dt>
                    <dd>
                      {addressSummary(order.billingAddress, copy.emptyDescription)}
                    </dd>
                  </div>
                </dl>
              </section>
            </>
          ) : null}
        </section>
      )}
    </PageShell>
  );
}
