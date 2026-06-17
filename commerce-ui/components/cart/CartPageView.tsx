"use client";

import { useMemo } from "react";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  DisabledAction,
  ReceiptFrame,
} from "@/components/ui/StorefrontPrimitives";
import { useCart } from "./CartProvider";
import type { CartItemResponse } from "@/lib/commerce/cart/types";

export type CartPageCopy = {
  eyebrow: string;
  title: string;
  description: string;
  checkoutDisabled: string;
  secondaryAction: string;
  summaryTitle: string;
  summaryNote: string;
  rowItems: string;
  rowShipping: string;
  rowShippingValue: string;
  rowDiscount: string;
  rowDiscountValue: string;
  totalLabel: string;
  emptyTitle: string;
  emptyDescription: string;
  loadingLabel: string;
  errorTitle: string;
  errorDescription: string;
  retryAction: string;
  clearAction: string;
  removeAction: string;
  decreaseAction: string;
  increaseAction: string;
  quantityLabel: string;
  unitPriceLabel: string;
  lineTotalLabel: string;
  priceChangedLabel: string;
  unavailableLabel: string;
  stockLabel: string;
  productFallback: string;
};

type CartPageViewProps = {
  copy: CartPageCopy;
  lang: string;
  storeHref: string;
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

const displayName = (item: CartItemResponse, fallback: string): string =>
  item.productSku || item.variantSku || item.productUid || fallback;

export function CartPageView({ copy, lang, storeHref }: CartPageViewProps) {
  const {
    cart,
    isLoading,
    isMutating,
    error,
    refresh,
    updateQuantity,
    removeItem,
    clearCart,
  } = useCart();
  const currencyIso = cart?.totals.currencyIso;
  const hasItems = Boolean(cart?.items.length);

  const summaryRows = useMemo(
    () => [
      {
        label: copy.rowItems,
        value: hasItems
          ? String(cart?.totals.itemCount ?? 0)
          : "0",
      },
      { label: copy.rowShipping, value: copy.rowShippingValue },
      { label: copy.rowDiscount, value: copy.rowDiscountValue },
    ],
    [
      cart?.totals.itemCount,
      copy.rowDiscount,
      copy.rowDiscountValue,
      copy.rowItems,
      copy.rowShipping,
      copy.rowShippingValue,
      hasItems,
    ],
  );

  return (
    <PageShell
      eyebrow={copy.eyebrow}
      title={copy.title}
      description={copy.description}
      actions={
        <>
          <DisabledAction label={copy.checkoutDisabled} />
          <ActionLink href={storeHref} label={copy.secondaryAction} variant="secondary" />
        </>
      }
      visual={
        <ReceiptFrame
          title={copy.summaryTitle}
          note={hasItems ? undefined : copy.summaryNote}
          rows={summaryRows}
          totalLabel={copy.totalLabel}
          totalValue={formatMoney(lang, currencyIso, cart?.totals.currentTotal)}
        />
      }
    >
      <section className="cart-panel surface-panel" aria-live="polite">
        {error ? (
          <div className="cart-status">
            <h2 className="frame-title">{copy.errorTitle}</h2>
            <p className="frame-note">{error || copy.errorDescription}</p>
            <button type="button" className="commerce-action mt-5" onClick={refresh}>
              {copy.retryAction}
            </button>
          </div>
        ) : null}

        {!error && isLoading ? (
          <div className="cart-status" role="status">
            <p className="frame-note">{copy.loadingLabel}</p>
          </div>
        ) : null}

        {!error && !isLoading && !hasItems ? (
          <div className="cart-status">
            <h2 className="frame-title">{copy.emptyTitle}</h2>
            <p className="frame-note">{copy.emptyDescription}</p>
          </div>
        ) : null}

        {!error && !isLoading && hasItems ? (
          <div className="cart-lines">
            {cart?.items.map((item) => (
              <article key={item.itemUid} className="cart-line">
                <div className="cart-line__main">
                  <h2 className="cart-line__title">
                    {displayName(item, copy.productFallback)}
                  </h2>
                  <p className="cart-line__meta">
                    {copy.unitPriceLabel}:{" "}
                    {formatMoney(lang, currencyIso, item.currentUnitPrice)}
                  </p>
                  <div className="cart-line__flags">
                    {item.priceChanged ? (
                      <span className="quiet-chip">{copy.priceChangedLabel}</span>
                    ) : null}
                    {!item.available ? (
                      <span className="quiet-chip">{copy.unavailableLabel}</span>
                    ) : null}
                    {item.stockQuantity !== null ? (
                      <span className="quiet-chip">
                        {copy.stockLabel}: {item.stockQuantity}
                      </span>
                    ) : null}
                  </div>
                </div>
                <div className="cart-line__controls">
                  <div
                    className="quantity-control"
                    aria-label={`${copy.quantityLabel}: ${item.quantity}`}
                  >
                    <button
                      type="button"
                      onClick={() => updateQuantity(item.itemUid, item.quantity - 1)}
                      disabled={isMutating || item.quantity <= 1}
                      aria-label={copy.decreaseAction}
                    >
                      -
                    </button>
                    <span>{item.quantity}</span>
                    <button
                      type="button"
                      onClick={() => updateQuantity(item.itemUid, item.quantity + 1)}
                      disabled={isMutating || item.quantity >= 99}
                      aria-label={copy.increaseAction}
                    >
                      +
                    </button>
                  </div>
                  <p className="cart-line__total">
                    <span>{copy.lineTotalLabel}</span>
                    <strong>{formatMoney(lang, currencyIso, item.lineTotal)}</strong>
                  </p>
                  <button
                    type="button"
                    className="cart-text-button"
                    onClick={() => removeItem(item.itemUid)}
                    disabled={isMutating}
                  >
                    {copy.removeAction}
                  </button>
                </div>
              </article>
            ))}
            <div className="cart-footer-actions">
              <button
                type="button"
                className="cart-text-button"
                onClick={clearCart}
                disabled={isMutating}
              >
                {copy.clearAction}
              </button>
            </div>
          </div>
        ) : null}
      </section>
    </PageShell>
  );
}
