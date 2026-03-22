import type { PageTemplateConfig } from "@/lib/types";
import CategoryPageTemplateConfig from "./configs/category-page.template";
import ContentPageTemplateConfig from "./configs/content-page.template";
import ErrorPageTemplateConfig from "./configs/error-page.template";
import LandingPageTemplateConfig from "./configs/landing-page.template";
import NotFoundPageTemplateConfig from "./configs/not-found-page.template";
import ProductDetailsPageTemplateConfig from "./configs/product-details-page.template";
import SearchResultsPageTemplateConfig from "./configs/search-results-page.template";

export type TemplateName =
  | "LandingPageTemplate"
  | "ContentPageTemplate"
  | "CategoryPageTemplate"
  | "ProductDetailsPageTemplate"
  | "SearchResultsPageTemplate"
  | "ErrorPageTemplate"
  | "NotFoundPageTemplate";

export const TEMPLATE_CONFIGS: Record<TemplateName, PageTemplateConfig> = {
  LandingPageTemplate: LandingPageTemplateConfig,
  ContentPageTemplate: ContentPageTemplateConfig,
  CategoryPageTemplate: CategoryPageTemplateConfig,
  ProductDetailsPageTemplate: ProductDetailsPageTemplateConfig,
  SearchResultsPageTemplate: SearchResultsPageTemplateConfig,
  ErrorPageTemplate: ErrorPageTemplateConfig,
  NotFoundPageTemplate: NotFoundPageTemplateConfig,
};
