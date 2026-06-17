import { getTranslations } from "next-intl/server";
import { notFound } from "next/navigation";
import {
  ProductDetailView,
  type ProductDetailCopy,
  type ProductDetailViewProduct,
} from "@/components/product/ProductDetailView";
import { getProductByUid } from "@/lib/commerce/product/product-client";
import type {
  ProductDeliveryResponse,
  ProductMediaDelivery,
} from "@/lib/commerce/product/types";
import { withLocalePath } from "@/lib/core/i18n/locale";

const mediaForDisplay = (
  product: ProductDeliveryResponse,
): ProductMediaDelivery | null =>
  product.mainImage?.desktop ??
  product.mainImage?.mobile ??
  product.gallery[0]?.desktop ??
  product.gallery[0]?.mobile ??
  null;

const optionLabelForVariant = (
  variant: ProductDeliveryResponse["variants"][number],
): string =>
  variant.optionValues
    .map((value) => value.valueLabel)
    .filter(Boolean)
    .join(" / ");

const toProductDetailViewProduct = (
  product: ProductDeliveryResponse,
): ProductDetailViewProduct => {
  const media = mediaForDisplay(product);

  return {
    uid: product.uid,
    sku: product.sku,
    name: product.name,
    description: product.shortDescription || product.description || "",
    priceFormatted: product.price.formattedValue,
    categories: product.categories
      .map((category) => category.name)
      .filter(Boolean),
    media: media
      ? {
          url: media.url,
          alt: product.name,
        }
      : null,
    variants: product.variants.map((variant) => ({
      uid: variant.uid,
      sku: variant.sku,
      priceFormatted: variant.price.formattedValue,
      stockQuantity: variant.stockQuantity,
      optionLabel: optionLabelForVariant(variant),
    })),
  };
};

export default async function ProductPage({
  params,
}: {
  params: Promise<{ lang: string; productUid: string }>;
}) {
  const { lang, productUid } = await params;
  const [translate, product] = await Promise.all([
    getTranslations("Product"),
    getProductByUid(productUid, lang),
  ]);

  if (!product) {
    notFound();
  }

  const copy: ProductDetailCopy = {
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
  };

  return (
    <ProductDetailView
      product={toProductDetailViewProduct(product)}
      copy={copy}
      storeHref={withLocalePath(lang)}
      cartHref={withLocalePath(lang, "cart")}
    />
  );
}
