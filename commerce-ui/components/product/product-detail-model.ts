export type ProductDetailModel = {
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

type Translator<TModel> = (key: keyof TModel & string) => string;

export const createProductDetailModel = (
  translate: Translator<ProductDetailModel>,
): ProductDetailModel => ({
  eyebrow: translate("eyebrow"),
  secondaryAction: translate("secondaryAction"),
  visualLabel: translate("visualLabel"),
  statusMedia: translate("statusMedia"),
  statusVariant: translate("statusVariant"),
  statusStock: translate("statusStock"),
  controlsTitle: translate("controlsTitle"),
  skuLabel: translate("skuLabel"),
  categoryLabel: translate("categoryLabel"),
  variantLabel: translate("variantLabel"),
  quantityLabel: translate("quantityLabel"),
  decreaseAction: translate("decreaseAction"),
  increaseAction: translate("increaseAction"),
  addToCartAction: translate("addToCartAction"),
  addingToCartAction: translate("addingToCartAction"),
  addedToCartTitle: translate("addedToCartTitle"),
  addedToCartDescription: translate("addedToCartDescription"),
  viewCartAction: translate("viewCartAction"),
  unavailableTitle: translate("unavailableTitle"),
  unavailableDescription: translate("unavailableDescription"),
  outOfStockLabel: translate("outOfStockLabel"),
  stockLabel: translate("stockLabel"),
  priceLabel: translate("priceLabel"),
  errorTitle: translate("errorTitle"),
  errorDescription: translate("errorDescription"),
});
