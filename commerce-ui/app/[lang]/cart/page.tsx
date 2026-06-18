import { getTranslations } from "next-intl/server";
import { CartPageView, type CartPageCopy } from "@/components/cart/CartPageView";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function CartPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Cart");
  const copy: CartPageCopy = {
    eyebrow: translate("eyebrow"),
    title: translate("title"),
    description: translate("description"),
    checkoutDisabled: translate("checkoutDisabled"),
    checkoutAction: translate("checkoutAction"),
    secondaryAction: translate("secondaryAction"),
    summaryTitle: translate("summaryTitle"),
    summaryNote: translate("summaryNote"),
    rowItems: translate("rowItems"),
    rowShipping: translate("rowShipping"),
    rowShippingValue: translate("rowShippingValue"),
    rowDiscount: translate("rowDiscount"),
    rowDiscountValue: translate("rowDiscountValue"),
    totalLabel: translate("totalLabel"),
    emptyTitle: translate("emptyTitle"),
    emptyDescription: translate("emptyDescription"),
    loadingLabel: translate("loadingLabel"),
    errorTitle: translate("errorTitle"),
    errorDescription: translate("errorDescription"),
    retryAction: translate("retryAction"),
    clearAction: translate("clearAction"),
    removeAction: translate("removeAction"),
    decreaseAction: translate("decreaseAction"),
    increaseAction: translate("increaseAction"),
    quantityLabel: translate("quantityLabel"),
    unitPriceLabel: translate("unitPriceLabel"),
    lineTotalLabel: translate("lineTotalLabel"),
    priceChangedLabel: translate("priceChangedLabel"),
    unavailableLabel: translate("unavailableLabel"),
    stockLabel: translate("stockLabel"),
    productFallback: translate("productFallback"),
  };

  return (
    <CartPageView
      copy={copy}
      lang={lang}
      storeHref={withLocalePath(lang)}
      checkoutHref={withLocalePath(lang, "checkout")}
    />
  );
}
