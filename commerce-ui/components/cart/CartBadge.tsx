"use client";

import { useCart } from "./CartProvider";

export function CartBadge({ label }: { label: string }) {
  const { cart } = useCart();
  const itemCount = cart?.totals.itemCount ?? 0;

  return (
    <>
      <span>{label}</span>
      <span className="cart-badge" aria-label={`${label}: ${itemCount}`}>
        {itemCount}
      </span>
    </>
  );
}
