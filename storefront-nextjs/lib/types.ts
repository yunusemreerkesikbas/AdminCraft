export type ApiResult = "SUCCESS" | "ERROR";

export const NavigationItemType = {
  URL: 'URL',
  PAGE: 'PAGE',
  COMPONENT: 'COMPONENT',
} as const;
export type NavigationItemType = typeof NavigationItemType[keyof typeof NavigationItemType];

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
  isExternal: boolean;
  responsive?: ResponsiveMediaDelivery | null;
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
  customFields?: Record<string, unknown> | null;
  navigationType?: NavigationType;
  searchBox?: boolean;
  navigationNode?: NavigationDeliveryResponse | null;
  navigationLinkNode?: NavigationDeliveryResponse | null;
  responsive?: ResponsiveMediaDelivery | null;
  entries: EntryDeliveryResponse[];
}

export interface NavigationEntryDeliveryResponse {
  uid: string;
  itemType: NavigationItemType;
  itemId?: string;
  url?: string;
  linkName?: string;
  linkColor?: string;
  target?: string;
  isExternal: boolean;
  resolvedHref?: string; // pre-computed by backend
}

export interface NavigationSectionDelivery {
  uid: string;
  title: string;
  href?: string;
  isExternal: boolean;
  links: LayoutLinkDelivery[];
}

export interface NavigationDeliveryResponse {
  uid: string;
  title?: string;
  position?: string;
  isTab?: boolean;
  entries?: NavigationEntryDeliveryResponse[];
  children?: NavigationDeliveryResponse[];
  sections?: NavigationSectionDelivery[];
  flatLinks?: LayoutLinkDelivery[];
}

export interface LayoutLinkDelivery {
  uid: string;
  label: string;
  href: string;
  isExternal: boolean;
  target?: string;
  color?: string;
}

export interface LayoutBlockDelivery {
  uid: string;
  role: string;
  componentType: string;
  title?: string;
  description?: string;
  navigationNode?: NavigationDeliveryResponse | null;
  links: LayoutLinkDelivery[];
  newsletterPlaceholder?: string;
  newsletterButtonLabel?: string;
}

export interface ShellDeliveryResponse {
  header: {
    mainNavigation?: (NavigationDeliveryResponse & { sections?: NavigationSectionDelivery[] }) | null;
    primaryBlocks: LayoutBlockDelivery[];
    secondaryBlocks: LayoutBlockDelivery[];
  };
  footer: {
    primaryBlocks: LayoutBlockDelivery[];
    bottomBlocks: LayoutBlockDelivery[];
  };
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



export interface SiteSeoDeliveryResponse {
  title?: string;
  description?: string;
  keywords?: string[];
  ogTitle?: string;
  ogDescription?: string;
  twitterCard?: string;
  titleSeparator?: string;
}

export interface SiteSearchEngineDeliveryResponse {
  sitemapEnabled: boolean;
  indexingEnabled: boolean;
  defaultRobots?: string;
}

export interface SiteContactDelivery {
  email?: string;
  phone?: string;
  whatsapp?: string;
}

export interface SiteAddressDelivery {
  line1?: string;
  line2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  mapEmbedUrl?: string;
}

export interface SiteSocialLinksDelivery {
  facebook?: string;
  instagram?: string;
  x?: string;
  linkedin?: string;
  youtube?: string;
  tiktok?: string;
}

export interface SiteI18nDelivery {
  siteName?: string;
  tagline?: string;
}

export interface CookieConsentDelivery {
  enabled: boolean;
  text?: string | null;
}

export interface SiteDeliveryResponse {
  siteName: string;
  ogImageUrl: string;
  seo?: SiteSeoDeliveryResponse;
  canonicalBaseUrl?: string;
  logoUrl?: string;
  logoDarkUrl?: string;
  defaultLanguage: string;
  enabledLanguages: LanguageInfo[];
  searchEngine?: SiteSearchEngineDeliveryResponse;
  themeName: string;
  maintenanceMode: boolean;
  maintenanceMessage: string;
  contact?: SiteContactDelivery;
  address?: SiteAddressDelivery;
  social?: SiteSocialLinksDelivery;
  i18n?: SiteI18nDelivery;
  cookieConsent?: CookieConsentDelivery;
}

export interface SitemapPageEntry {
  uid: string;
  typeCode: string;
  canonicalUrl: string;
  updatedAt: string;
}

export interface PriceResponse {
  currencyIso: string;
  formattedValue: string;
  priceType: string;
  value: number;
}


export interface CmsMediaDelivery {
  uid: string;
  publicUrl?: string;
  url?: string;
  mimeType?: string;
  width?: number;
  height?: number;
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
  shell?: { header?: boolean; footer?: boolean };
  slots: TemplateSlotConfig[];
}
