import { getTranslations } from "next-intl/server";
import { notFound } from "next/navigation";
import {
  ProductDetailView,
  type ProductDetailViewProduct,
} from "@/components/product/ProductDetailView";
import { createProductDetailModel } from "@/components/product/product-detail-model";
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

  const model = createProductDetailModel(translate);

  return (
    <ProductDetailView
      product={toProductDetailViewProduct(product)}
      model={model}
      storeHref={withLocalePath(lang)}
      cartHref={withLocalePath(lang, "cart")}
    />
  );
}
