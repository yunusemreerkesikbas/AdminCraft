export type PriceResponse = {
  currencyIso: string;
  formattedValue: string;
  priceType: string;
  value: number | string;
};

export type ProductMediaDelivery = {
  uid: string;
  url: string;
  mimeType: string | null;
  width: number | null;
  height: number | null;
};

export type ResponsiveMediaDelivery = {
  uid: string;
  desktop: ProductMediaDelivery | null;
  mobile: ProductMediaDelivery | null;
};

export type ProductCategoryDelivery = {
  uid: string;
  code: string;
  name: string;
  isPrimary: boolean;
};

export type ProductAttributeDelivery = {
  code: string;
  name: string;
  fieldType: string;
  value: unknown;
};

export type VariantOptionValueDelivery = {
  optionCode: string;
  optionName: string;
  displayType: string;
  valueCode: string;
  valueLabel: string;
  swatchValue: string | null;
};

export type VariantDelivery = {
  uid: string;
  sku: string;
  price: PriceResponse;
  firstPrice: PriceResponse | null;
  vatRate: string | null;
  stockQuantity: number;
  optionValues: VariantOptionValueDelivery[];
};

export type ProductDeliveryResponse = {
  uid: string;
  sku: string;
  name: string;
  shortDescription: string | null;
  description: string | null;
  price: PriceResponse;
  seoTitle: string | null;
  seoDescription: string | null;
  productTypeName: string | null;
  mainImage: ResponsiveMediaDelivery | null;
  attributes: ProductAttributeDelivery[];
  categories: ProductCategoryDelivery[];
  gallery: ResponsiveMediaDelivery[];
  variants: VariantDelivery[];
};
