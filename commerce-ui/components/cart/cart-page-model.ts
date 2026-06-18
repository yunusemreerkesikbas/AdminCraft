export type CartPageModel = {
  eyebrow: string;
  title: string;
  description: string;
  checkoutDisabled: string;
  checkoutAction: string;
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

type Translator = (key: string) => string;

export const createCartPageModel = (
  translate: Translator,
): CartPageModel => ({
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
});
