"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { InlineCustomerAuthGate } from "@/components/customer/InlineCustomerAuthGate";
import { useCustomerSession } from "@/components/customer/CustomerSessionProvider";
import { PageShell } from "@/components/ui/PageShell";
import { ActionLink, ReceiptFrame } from "@/components/ui/StorefrontPrimitives";
import { createCommerceOrderClient } from "@/lib/commerce/order/order-client";
import type {
  CommerceOrderDetailResponse,
  CommerceOrderLegalSnapshot,
  CommerceOrderLegalSnapshotDocument,
  CommerceOrderResolutionRequestResponse,
  CommerceOrderResolutionRequestType,
  CommerceOrderSummaryResponse,
} from "@/lib/commerce/order/types";
import { withLocalePath } from "@/lib/core/i18n/locale";
import type { OrdersModel } from "./orders-model";

type OrdersViewProps = {
  model: OrdersModel;
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

const eligibleRequestType = (
  status: string,
): CommerceOrderResolutionRequestType | null => {
  if (status === "PAID" || status === "PREPARING") {
    return "CANCELLATION";
  }
  if (status === "DELIVERED") {
    return "RETURN";
  }
  return null;
};

const isLegalSnapshotDocument = (
  value: unknown,
): value is CommerceOrderLegalSnapshotDocument => {
  if (!value || typeof value !== "object") {
    return false;
  }
  const document = value as Record<string, unknown>;
  return (
    typeof document.templateUid === "string" &&
    typeof document.type === "string" &&
    typeof document.language === "string" &&
    typeof document.version === "number" &&
    typeof document.title === "string" &&
    typeof document.contentText === "string" &&
    typeof document.contentHash === "string"
  );
};

const legalSnapshotDocuments = (
  order: CommerceOrderDetailResponse | null,
): CommerceOrderLegalSnapshotDocument[] => {
  if (!order?.legalSnapshotJson) {
    return [];
  }

  try {
    const snapshot = JSON.parse(order.legalSnapshotJson) as Partial<CommerceOrderLegalSnapshot>;
    if (!Array.isArray(snapshot.documents)) {
      return [];
    }
    return snapshot.documents.filter(isLegalSnapshotDocument);
  } catch {
    return [];
  }
};

export function OrdersView({
  model,
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
      eyebrow={model.eyebrow}
      title={model.title}
      description={model.description}
      visual={
        <ReceiptFrame
          title={model.listTitle}
          note={
            isLoading ? model.loadingLabel : currentError || model.emptyDescription
          }
          rows={[
            { label: model.rowItems, value: String(visibleOrders.length) },
            {
              label: model.rowTotal,
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
          <p className="row-title">{model.loadingLabel}</p>
        </section>
      ) : !isAuthenticated ? (
        <InlineCustomerAuthGate model={model.auth} source="commerce-ui-orders" />
      ) : (
        <section className="orders-panel">
          {currentError ? (
            <p className="account-form-error" role="alert">
              {currentError || model.errorFallback}
            </p>
          ) : null}
          {visibleOrders.length === 0 && !isLoading ? (
            <article className="surface-panel order-empty">
              <h2 className="frame-title">{model.emptyTitle}</h2>
              <p className="frame-note">{model.emptyDescription}</p>
            </article>
          ) : null}
          {visibleOrders.map((order) => (
            <article key={order.orderUid} className="surface-panel order-card">
              <div>
                <span className="quiet-chip">{order.status}</span>
              </div>
              <div>
                <h2 className="frame-title">{order.orderNumber}</h2>
                <p className="frame-note">{formatDate(lang, order.createdAt)}</p>
              </div>
              <dl className="order-meta">
                <div>
                  <dt>{model.rowItems}</dt>
                  <dd>{order.itemCount}</dd>
                </div>
                <div>
                  <dt>{model.rowTotal}</dt>
                  <dd>
                    {formatMoney(lang, order.currencyIso, order.totals.total)}
                  </dd>
                </div>
              </dl>
              <Link
                className="commerce-action commerce-action--secondary"
                href={withLocalePath(lang, `account/orders/${order.orderUid}`)}
              >
                {model.detailsAction}
              </Link>
            </article>
          ))}
        </section>
      )}
    </PageShell>
  );
}

export function OrderDetailView({
  model,
  apiBaseUrl,
  lang,
  tenantHeaders,
  orderUid,
}: OrderDetailViewProps) {
  const { accessToken, isAuthenticated, isRestoring } = useCustomerSession();
  const [order, setOrder] = useState<CommerceOrderDetailResponse | null>(null);
  const [requests, setRequests] = useState<CommerceOrderResolutionRequestResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [requestError, setRequestError] = useState<string | null>(null);
  const [requestSuccess, setRequestSuccess] = useState<string | null>(null);
  const [isSubmittingRequest, setIsSubmittingRequest] = useState(false);
  const [requestReason, setRequestReason] = useState("");
  const [requestDescription, setRequestDescription] = useState("");
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
    Promise.all([
      orderClient.getOrder(accessToken, orderUid),
      orderClient.listOrderRequests(accessToken, orderUid),
    ])
      .then(([nextOrder, nextRequests]) => {
        if (!cancelled) {
          setOrder(nextOrder);
          setRequests(nextRequests);
          setError(null);
          setRequestError(null);
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
  const currentError =
    loadState.key === requestKey && loadState.status === "error" ? error : null;
  const visibleOrder =
    loadState.key === requestKey && loadState.status === "loaded" ? order : null;
  const visibleRequests =
    loadState.key === requestKey && loadState.status === "loaded" ? requests : [];
  const pendingRequest = visibleRequests.find((request) => request.status === "PENDING");
  const allowedRequestType = visibleOrder ? eligibleRequestType(visibleOrder.status) : null;
  const visibleLegalDocuments = legalSnapshotDocuments(visibleOrder);
  const isLoading =
    !isRestoring &&
    Boolean(isAuthenticated && accessToken) &&
    loadState.key !== requestKey;

  const submitResolutionRequest = async () => {
    if (!accessToken || !visibleOrder || !allowedRequestType || isSubmittingRequest) {
      return;
    }
    setIsSubmittingRequest(true);
    setRequestError(null);
    setRequestSuccess(null);
    try {
      const created = await orderClient.createOrderRequest(
        accessToken,
        visibleOrder.orderUid,
        {
          requestType: allowedRequestType,
          reason: requestReason,
          description: requestDescription,
        },
      );
      setRequests((current) => [created, ...current]);
      setOrder((current) =>
        current
          ? {
              ...current,
              status: created.requestedOrderStatus,
            }
          : current,
      );
      setRequestReason("");
      setRequestDescription("");
      setRequestSuccess(model.requestSuccessLabel);
    } catch (err) {
      setRequestError(err instanceof Error ? err.message : model.errorFallback);
    } finally {
      setIsSubmittingRequest(false);
    }
  };

  return (
    <PageShell
      eyebrow={model.eyebrow}
      title={visibleOrder?.orderNumber ?? model.detailTitle}
      description={isLoading ? model.loadingLabel : model.description}
      actions={
        <ActionLink
          href={withLocalePath(lang, "account/orders")}
          label={model.primaryAction}
          variant="secondary"
        />
      }
      visual={
        <ReceiptFrame
          title={model.detailTitle}
          note={currentError ? currentError || model.errorFallback : undefined}
          rows={[
            { label: model.rowOrderUid, value: visibleOrder?.orderUid ?? orderUid },
            { label: model.rowStatus, value: visibleOrder?.status ?? "-" },
            {
              label: model.rowCreatedAt,
              value: visibleOrder ? formatDate(lang, visibleOrder.createdAt) : "-",
            },
            { label: model.rowItems, value: String(visibleOrder?.itemCount ?? 0) },
            {
              label: model.rowLegal,
              value: visibleOrder?.legalSnapshotStatus ?? "-",
            },
          ]}
          totalLabel={model.rowTotal}
          totalValue={
            visibleOrder
              ? formatMoney(lang, visibleOrder.currencyIso, visibleOrder.totals.total)
              : "-"
          }
        />
      }
    >
      {isRestoring ? (
        <section className="surface-panel account-loading">
          <p className="row-title">{model.loadingLabel}</p>
        </section>
      ) : !isAuthenticated ? (
        <InlineCustomerAuthGate
          model={model.auth}
          source="commerce-ui-order-detail"
        />
      ) : (
        <section className="orders-panel">
          {currentError ? (
            <p className="account-form-error" role="alert">
              {currentError || model.errorFallback}
            </p>
          ) : null}
          {visibleOrder ? (
            <>
              <section className="surface-panel order-detail-panel">
                <h2 className="frame-title">{model.rowItems}</h2>
                <div className="checkout-items">
                  {visibleOrder.items.map((item) => (
                    <article key={item.uid} className="checkout-item-row">
                      <span>
                        {item.productSku ||
                          item.variantSku ||
                          item.productUid ||
                          model.itemFallback}
                      </span>
                      <strong>
                        {item.quantity} x{" "}
                        {formatMoney(
                          lang,
                          visibleOrder.currencyIso,
                          item.unitGrossPrice,
                        )}
                      </strong>
                    </article>
                  ))}
                </div>
              </section>
              <section className="surface-panel order-detail-panel">
                <h2 className="frame-title">{model.rowShipping}</h2>
                <dl className="order-meta order-meta--stacked">
                  <div>
                    <dt>{model.rowShipping}</dt>
                    <dd>{visibleOrder.shipping.methodNameKey}</dd>
                  </div>
                  <div>
                    <dt>{model.rowDelivery}</dt>
                    <dd>
                      {addressSummary(
                        visibleOrder.deliveryAddress,
                        model.emptyDescription,
                      )}
                    </dd>
                  </div>
                  <div>
                    <dt>{model.rowBilling}</dt>
                    <dd>
                      {addressSummary(
                        visibleOrder.billingAddress,
                        model.emptyDescription,
                      )}
                    </dd>
                  </div>
                </dl>
              </section>
              <section className="surface-panel order-detail-panel">
                <h2 className="frame-title">{model.rowLegalDocuments}</h2>
                {visibleLegalDocuments.length > 0 ? (
                  <div className="checkout-legal__documents">
                    {visibleLegalDocuments.map((document) => (
                      <article
                        key={`${document.templateUid}:${document.version}`}
                        className="checkout-legal__document"
                      >
                        <div className="checkout-legal__header">
                          <div>
                            <h3 className="row-title">{document.title}</h3>
                            <p className="frame-note">
                              {document.type} / {model.rowLegalVersion}{" "}
                              {document.version}
                            </p>
                          </div>
                          <span className="quiet-chip">
                            {document.contentHash.slice(0, 12)}
                          </span>
                        </div>
                        <pre className="checkout-legal__content">
                          {document.contentText}
                        </pre>
                      </article>
                    ))}
                  </div>
                ) : (
                  <p className="frame-note">{model.rowLegalUnavailable}</p>
                )}
              </section>
              <section className="surface-panel order-detail-panel">
                <h2 className="frame-title">{model.requestStatusTitle}</h2>
                {visibleRequests.length > 0 ? (
                  <div className="checkout-items">
                    {visibleRequests.map((request) => (
                      <article key={request.requestUid} className="checkout-item-row">
                        <span>
                          {request.type} / {request.status}
                        </span>
                        <strong>{request.refundStatus}</strong>
                      </article>
                    ))}
                  </div>
                ) : null}
                {requestSuccess ? (
                  <p className="account-form-success">{requestSuccess}</p>
                ) : null}
                {requestError ? (
                  <p className="account-form-error" role="alert">
                    {requestError}
                  </p>
                ) : null}
                {pendingRequest ? null : allowedRequestType ? (
                  <form
                    className="account-form"
                    onSubmit={(event) => {
                      event.preventDefault();
                      void submitResolutionRequest();
                    }}
                  >
                    <label className="account-form-label">
                      <span>{model.requestReasonLabel}</span>
                      <input
                        value={requestReason}
                        maxLength={100}
                        required
                        onChange={(event) => setRequestReason(event.target.value)}
                      />
                    </label>
                    <label className="account-form-label">
                      <span>{model.requestDescriptionLabel}</span>
                      <textarea
                        value={requestDescription}
                        maxLength={1000}
                        required
                        rows={4}
                        onChange={(event) =>
                          setRequestDescription(event.target.value)
                        }
                      />
                    </label>
                    <button
                      className="commerce-action"
                      type="submit"
                      disabled={isSubmittingRequest}
                    >
                      {allowedRequestType === "CANCELLATION"
                        ? model.requestCancellationAction
                        : model.requestReturnAction}
                    </button>
                  </form>
                ) : null}
              </section>
            </>
          ) : null}
        </section>
      )}
    </PageShell>
  );
}
