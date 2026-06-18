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
import type { CartPageModel } from "./cart-page-model";

type CartPageViewProps = {
  model: CartPageModel;
  lang: string;
  storeHref: string;
  checkoutHref: string;
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

export function CartPageView({
  model,
  lang,
  storeHref,
  checkoutHref,
}: CartPageViewProps) {
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
        label: model.rowItems,
        value: hasItems
          ? String(cart?.totals.itemCount ?? 0)
          : "0",
      },
      { label: model.rowShipping, value: model.rowShippingValue },
      { label: model.rowDiscount, value: model.rowDiscountValue },
    ],
    [
      cart?.totals.itemCount,
      model.rowDiscount,
      model.rowDiscountValue,
      model.rowItems,
      model.rowShipping,
      model.rowShippingValue,
      hasItems,
    ],
  );

  return (
    <PageShell
      eyebrow={model.eyebrow}
      title={model.title}
      description={model.description}
      actions={
        <>
          {hasItems && !isLoading && !error ? (
            <ActionLink href={checkoutHref} label={model.checkoutAction} />
          ) : (
            <DisabledAction label={model.checkoutDisabled} />
          )}
          <ActionLink href={storeHref} label={model.secondaryAction} variant="secondary" />
        </>
      }
      visual={
        <ReceiptFrame
          title={model.summaryTitle}
          note={hasItems ? undefined : model.summaryNote}
          rows={summaryRows}
          totalLabel={model.totalLabel}
          totalValue={formatMoney(lang, currencyIso, cart?.totals.currentTotal)}
        />
      }
    >
      <section className="cart-panel surface-panel" aria-live="polite">
        {error ? (
          <div className="cart-status">
            <h2 className="frame-title">{model.errorTitle}</h2>
            <p className="frame-note">{error || model.errorDescription}</p>
            <button type="button" className="commerce-action mt-5" onClick={refresh}>
              {model.retryAction}
            </button>
          </div>
        ) : null}

        {!error && isLoading ? (
          <div className="cart-status" role="status">
            <p className="frame-note">{model.loadingLabel}</p>
          </div>
        ) : null}

        {!error && !isLoading && !hasItems ? (
          <div className="cart-status">
            <h2 className="frame-title">{model.emptyTitle}</h2>
            <p className="frame-note">{model.emptyDescription}</p>
          </div>
        ) : null}

        {!error && !isLoading && hasItems ? (
          <div className="cart-lines">
            {cart?.items.map((item) => (
              <article key={item.itemUid} className="cart-line">
                <div className="cart-line__main">
                  <h2 className="cart-line__title">
                    {displayName(item, model.productFallback)}
                  </h2>
                  <p className="cart-line__meta">
                    {model.unitPriceLabel}:{" "}
                    {formatMoney(lang, currencyIso, item.currentUnitPrice)}
                  </p>
                  <div className="cart-line__flags">
                    {item.priceChanged ? (
                      <span className="quiet-chip">{model.priceChangedLabel}</span>
                    ) : null}
                    {!item.available ? (
                      <span className="quiet-chip">{model.unavailableLabel}</span>
                    ) : null}
                    {item.stockQuantity !== null ? (
                      <span className="quiet-chip">
                        {model.stockLabel}: {item.stockQuantity}
                      </span>
                    ) : null}
                  </div>
                </div>
                <div className="cart-line__controls">
                  <div
                    className="quantity-control"
                    aria-label={`${model.quantityLabel}: ${item.quantity}`}
                  >
                    <button
                      type="button"
                      onClick={() => updateQuantity(item.itemUid, item.quantity - 1)}
                      disabled={isMutating || item.quantity <= 1}
                      aria-label={model.decreaseAction}
                    >
                      -
                    </button>
                    <span>{item.quantity}</span>
                    <button
                      type="button"
                      onClick={() => updateQuantity(item.itemUid, item.quantity + 1)}
                      disabled={isMutating || item.quantity >= 99}
                      aria-label={model.increaseAction}
                    >
                      +
                    </button>
                  </div>
                  <p className="cart-line__total">
                    <span>{model.lineTotalLabel}</span>
                    <strong>{formatMoney(lang, currencyIso, item.lineTotal)}</strong>
                  </p>
                  <button
                    type="button"
                    className="cart-text-button"
                    onClick={() => removeItem(item.itemUid)}
                    disabled={isMutating}
                  >
                    {model.removeAction}
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
                {model.clearAction}
              </button>
            </div>
          </div>
        ) : null}
      </section>
    </PageShell>
  );
}
