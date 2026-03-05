export type ApiResult = "SUCCESS" | "ERROR";

export enum NavigationType {
  MAINMENU = "MAINMENU",
  STATICPAGE = "STATICPAGE",
}

export interface ApiResponse<T> {
  result: ApiResult;
  message: string;
  data: T;
  code?: number | null;
}

export interface EntryDeliveryResponse {
  uid: string;
  order: number;
  title: string;
  description?: string;
  isVisible: boolean;
  styleClasses?: string;
  customFields: Record<string, unknown>;
}

export interface ComponentDeliveryResponse {
  uid: string;
  type: string;
  category: string;
  title: string;
  subtitle?: string;
  description?: string;
  isVisible: boolean;
  styleClasses?: string;
  navigationType?: NavigationType;
  searchBox?: boolean;
  navigationNode?: NavigationDeliveryResponse | null;
  navigationLinkNode?: NavigationDeliveryResponse | null;
  entries: EntryDeliveryResponse[];
}

export interface NavigationEntryDeliveryResponse {
  uid: string;
  itemType: string;
  itemId?: string;
  url?: string;
  linkName?: string;
  linkColor?: string;
  target?: string;
  isExternal?: boolean;
}

export interface NavigationDeliveryResponse {
  uid: string;
  title?: string;
  position?: string;
  isTab?: boolean;
  entries?: NavigationEntryDeliveryResponse[];
  children?: NavigationDeliveryResponse[];
}

export interface ContentSlotDeliveryResponse {
  slotId: string;
  slotUuid: string;
  position: string;
  name: string;
  slotShared: boolean;
  components: { component: ComponentDeliveryResponse[] };
}

export interface ContentSlotsWrapper {
  contentSlot: ContentSlotDeliveryResponse[];
}

export interface PageDeliveryResponse {
  uid: string;
  name: string;
  title: string;
  description: string;
  robotTag: string;
  canonicalUrl: string;
  styleClasses: string;
  template: string;
  typeCode: string;
  contentSlots?: ContentSlotsWrapper;
}

export interface LanguageInfo {
  code: string;
  nativeName: string;
  isRtl: boolean;
}

export interface TenantLanguage {
  code: string;
  nativeName: string;
  englishName: string;
  rtl: boolean;
}

export interface TenantConfigResponse {
  id: number;
  subdomain: string;
  defaultLanguage: string;
  supportedLanguages: TenantLanguage[];
  currency: string;
  status: string;
}

export interface SiteDeliveryResponse {
  siteName: string;
  siteTitle: string;
  siteDescription: string;
  siteKeywords: string;
  ogImageUrl: string;
  defaultLanguage: string;
  enabledLanguages: LanguageInfo[];
  themeName: string;
  maintenanceMode: boolean;
  maintenanceMessage: string;
  googleAnalyticsId: string;
  googleTagManagerId: string;
  twitterHandle: string;
  facebookPageUrl: string;
  domain: string;
  customDomain: string;
  sslEnabled: boolean;
}

export interface PriceResponse {
  currencyIso: string;
  formattedValue: string;
  priceType: string;
  value: number;
}

export interface MediaDelivery {
  uid: string;
  url: string;
  mimeType?: string;
  width?: number;
  height?: number;
}

export interface ResponsiveMediaDelivery {
  uid?: string;
  desktop?: MediaDelivery | null;
  mobile?: MediaDelivery | null;
}

export interface AttributeDelivery {
  code: string;
  name: string;
  fieldType: string;
  value: unknown;
}

export interface CategoryDelivery {
  uid: string;
  code: string;
  name: string;
  isPrimary?: boolean;
}

export interface ProductDeliveryResponse {
  uid: string;
  sku: string;
  name: string;
  shortDescription?: string;
  description?: string;
  price: PriceResponse;
  seoTitle?: string;
  seoDescription?: string;
  productTypeName?: string;
  mainImage?: ResponsiveMediaDelivery | null;
  attributes?: AttributeDelivery[];
  categories?: CategoryDelivery[];
  gallery?: ResponsiveMediaDelivery[];
}

export interface ProductListDeliveryResponse {
  uid: string;
  sku: string;
  name: string;
  shortDescription?: string;
  basePrice?: number;
  thumbnailUrl?: string;
  productTypeName?: string;
}

export interface CategoryDeliveryResponse {
  uid: string;
  code: string;
  name: string;
  description?: string;
  children?: CategoryDeliveryResponse[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
}

export type SlotPosition = 'TOP' | 'CENTER' | 'BOTTOM' | 'LEFT' | 'RIGHT';

export interface TemplateSlotConfig {
  slotName: string;
  position: SlotPosition;
  className?: string;
}

export interface PageTemplateConfig {
  templateClass?: string;
  chrome?: { header?: boolean; footer?: boolean };
  slots: TemplateSlotConfig[];
}
