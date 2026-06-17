"use client";

import Image from "next/image";
import { useMemo, useState } from "react";
import { useCart } from "@/components/cart/CartProvider";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  ProductFrame,
} from "@/components/ui/StorefrontPrimitives";

export type ProductDetailCopy = {
  eyebrow: string;
  secondaryAction: string;
  visualLabel: string;
  statusMedia: string;
  statusVariant: string;
  statusStock: string;
  controlsTitle: string;
  skuLabel: string;
  categoryLabel: string;
  variantLabel: string;
  quantityLabel: string;
  decreaseAction: string;
  increaseAction: string;
  addToCartAction: string;
  addingToCartAction: string;
  addedToCartTitle: string;
  addedToCartDescription: string;
  viewCartAction: string;
  unavailableTitle: string;
  unavailableDescription: string;
  outOfStockLabel: string;
  stockLabel: string;
  priceLabel: string;
  errorTitle: string;
  errorDescription: string;
};

export type ProductDetailMedia = {
  url: string;
  alt: string;
};

export type ProductDetailVariant = {
  uid: string;
  sku: string;
  priceFormatted: string;
  stockQuantity: number;
  optionLabel: string;
};

export type ProductDetailViewProduct = {
  uid: string;
  sku: string;
  name: string;
  description: string;
  priceFormatted: string;
  categories: string[];
  media: ProductDetailMedia | null;
  variants: ProductDetailVariant[];
};

type ProductDetailViewProps = {
  product: ProductDetailViewProduct;
  copy: ProductDetailCopy;
  storeHref: string;
  cartHref: string;
};

const findInitialVariant = (
  variants: ProductDetailVariant[],
): ProductDetailVariant | null =>
  variants.find((variant) => variant.stockQuantity > 0) ?? variants[0] ?? null;

const variantLabel = (variant: ProductDetailVariant): string =>
  variant.optionLabel || variant.sku;

const clampQuantity = (quantity: number, maxQuantity: number): number =>
  Math.min(Math.max(quantity, 1), Math.max(maxQuantity, 1));

function ProductMedia({
  product,
  copy,
}: {
  product: ProductDetailViewProduct;
  copy: ProductDetailCopy;
}) {
  if (!product.media) {
    return (
      <ProductFrame
        label={copy.visualLabel}
        status={[copy.statusMedia, copy.statusVariant, copy.statusStock]}
      />
    );
  }

  return (
    <aside className="visual-frame product-media-frame" aria-label={copy.visualLabel}>
      <Image
        src={product.media.url}
        alt={product.media.alt}
        fill
        priority
        sizes="(max-width: 768px) 100vw, 38vw"
        className="product-media-frame__image"
      />
    </aside>
  );
}

export function ProductDetailView({
  product,
  copy,
  storeHref,
  cartHref,
}: ProductDetailViewProps) {
  const { addCartItem, isMutating, error: cartError } = useCart();
  const initialVariant = useMemo(
    () => findInitialVariant(product.variants),
    [product.variants],
  );
  const [selectedVariantUid, setSelectedVariantUid] = useState(
    initialVariant?.uid ?? "",
  );
  const [quantity, setQuantity] = useState(1);
  const [wasAdded, setWasAdded] = useState(false);
  const [addFailed, setAddFailed] = useState(false);

  const selectedVariant =
    product.variants.find((variant) => variant.uid === selectedVariantUid) ??
    initialVariant;
  const maxQuantity = selectedVariant
    ? Math.min(99, selectedVariant.stockQuantity)
    : 0;
  const canAddToCart = Boolean(selectedVariant && maxQuantity > 0);
  const effectiveQuantity = clampQuantity(quantity, maxQuantity);
  const categories = product.categories.join(", ");

  const handleAddToCart = async () => {
    if (!selectedVariant || !canAddToCart) {
      return;
    }

    const added = await addCartItem(selectedVariant.uid, effectiveQuantity);
    setWasAdded(added);
    setAddFailed(!added);
  };

  return (
    <PageShell
      eyebrow={copy.eyebrow}
      title={product.name}
      description={product.description}
      actions={
        <>
          <ActionLink href={cartHref} label={copy.viewCartAction} />
          <ActionLink href={storeHref} label={copy.secondaryAction} variant="secondary" />
        </>
      }
      visual={<ProductMedia product={product} copy={copy} />}
    >
      <section className="surface-panel product-purchase-panel">
        <div className="product-purchase-panel__header">
          <div>
            <h2 className="frame-title">{copy.controlsTitle}</h2>
            <p className="frame-note">
              {copy.skuLabel}: {product.sku}
            </p>
          </div>
          <div className="product-price-block">
            <span>{copy.priceLabel}</span>
            <strong>{selectedVariant?.priceFormatted ?? product.priceFormatted}</strong>
          </div>
        </div>

        {categories ? (
          <p className="product-meta-line">
            {copy.categoryLabel}: {categories}
          </p>
        ) : null}

        {product.variants.length > 0 ? (
          <div className="product-variant-group" aria-label={copy.variantLabel}>
            <p className="row-title">{copy.variantLabel}</p>
            <div className="product-variant-options">
              {product.variants.map((variant) => {
                const isSelected = variant.uid === selectedVariant?.uid;
                const isAvailable = variant.stockQuantity > 0;

                return (
                  <button
                    key={variant.uid}
                    type="button"
                    className="product-variant-button"
                    aria-pressed={isSelected}
                    onClick={() => {
                      setSelectedVariantUid(variant.uid);
                      setWasAdded(false);
                      setAddFailed(false);
                    }}
                  >
                    <span>{variantLabel(variant)}</span>
                    <span className="row-description">
                      {isAvailable
                        ? `${copy.stockLabel}: ${variant.stockQuantity}`
                        : copy.outOfStockLabel}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>
        ) : (
          <div className="product-attention">
            <h3 className="row-title">{copy.unavailableTitle}</h3>
            <p className="row-description">{copy.unavailableDescription}</p>
          </div>
        )}

        <div className="product-purchase-actions">
          <div
            className="quantity-control"
            aria-label={`${copy.quantityLabel}: ${effectiveQuantity}`}
          >
            <button
              type="button"
              onClick={() => {
                setQuantity(clampQuantity(effectiveQuantity - 1, maxQuantity));
                setWasAdded(false);
                setAddFailed(false);
              }}
              disabled={!canAddToCart || effectiveQuantity <= 1}
              aria-label={copy.decreaseAction}
            >
              -
            </button>
            <span>{effectiveQuantity}</span>
            <button
              type="button"
              onClick={() => {
                setQuantity(clampQuantity(effectiveQuantity + 1, maxQuantity));
                setWasAdded(false);
                setAddFailed(false);
              }}
              disabled={!canAddToCart || effectiveQuantity >= maxQuantity}
              aria-label={copy.increaseAction}
            >
              +
            </button>
          </div>
          <button
            type="button"
            className="commerce-action"
            onClick={handleAddToCart}
            disabled={!canAddToCart || isMutating}
          >
            {isMutating ? copy.addingToCartAction : copy.addToCartAction}
          </button>
        </div>

        {!canAddToCart ? (
          <p className="product-attention row-description">
            {copy.unavailableDescription}
          </p>
        ) : null}

        {wasAdded ? (
          <div className="product-confirmation" role="status">
            <strong>{copy.addedToCartTitle}</strong>
            <span>{copy.addedToCartDescription}</span>
            <ActionLink href={cartHref} label={copy.viewCartAction} variant="secondary" />
          </div>
        ) : null}

        {cartError || addFailed ? (
          <div className="product-attention" role="alert">
            <h3 className="row-title">{copy.errorTitle}</h3>
            <p className="row-description">{cartError || copy.errorDescription}</p>
          </div>
        ) : null}
      </section>
    </PageShell>
  );
}
