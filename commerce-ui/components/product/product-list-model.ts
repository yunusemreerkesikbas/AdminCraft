export type ProductListModel = {
  searchLabel: string;
  searchPlaceholder: string;
  searchAction: string;
  clearAction: string;
  resultsLabel: string;
  emptyTitle: string;
  emptyDescription: string;
  productTypeFallback: string;
  priceLabel: string;
  detailsAction: string;
  previousAction: string;
  nextAction: string;
  pageLabel: string;
  imageAltFallback: string;
  errorTitle: string;
  errorDescription: string;
  retryAction: string;
};

type Translator<TModel> = (key: keyof TModel & string) => string;

export const createProductListModel = (
  translate: Translator<ProductListModel>,
): ProductListModel => ({
  searchLabel: translate("searchLabel"),
  searchPlaceholder: translate("searchPlaceholder"),
  searchAction: translate("searchAction"),
  clearAction: translate("clearAction"),
  resultsLabel: translate("resultsLabel"),
  emptyTitle: translate("emptyTitle"),
  emptyDescription: translate("emptyDescription"),
  productTypeFallback: translate("productTypeFallback"),
  priceLabel: translate("priceLabel"),
  detailsAction: translate("detailsAction"),
  previousAction: translate("previousAction"),
  nextAction: translate("nextAction"),
  pageLabel: translate("pageLabel"),
  imageAltFallback: translate("imageAltFallback"),
  errorTitle: translate("errorTitle"),
  errorDescription: translate("errorDescription"),
  retryAction: translate("retryAction"),
});
