import React from "react";
import { ComponentDeliveryResponse } from "@/lib/types";
import UnknownComponent from "./UnknownComponent";
import HeaderCmsComponent from "@/components/cms/navigation/HeaderCmsComponent";
import FooterCmsComponent from "@/components/cms/navigation/FooterCmsComponent";
import NavigationCmsComponent from "@/components/cms/navigation/NavigationCmsComponent";
import PortfolioGridRenderer from "./PortfolioGridRenderer";

export type CmsComponentProps = {
  component: ComponentDeliveryResponse;
  lang?: string;
};

function TextBlockRenderer({ component }: CmsComponentProps) {
  return React.createElement(
    "article",
    { className: "space-y-2 rounded-lg border border-slate-200 p-4" },
    React.createElement(
      "h3",
      { className: "text-lg font-semibold text-slate-900" },
      component.title || component.uid,
    ),
    component.subtitle
      ? React.createElement("p", { className: "text-sm text-slate-600" }, component.subtitle)
      : null,
    component.description
      ? React.createElement("p", { className: "text-sm text-slate-700" }, component.description)
      : null,
  );
}

const registry: Record<string, React.ComponentType<CmsComponentProps>> = {
  HeaderComponent: HeaderCmsComponent,
  FooterComponent: FooterCmsComponent,
  NavigationComponent: NavigationCmsComponent,
  SimpleBannerComponent: TextBlockRenderer,
  CMSImageComponent: TextBlockRenderer,
  CMSLinkComponent: TextBlockRenderer,
  CMSParagraphComponent: TextBlockRenderer,
  RotatingImagesComponent: TextBlockRenderer,
  FeatureCardComponent: PortfolioGridRenderer,
  CustomerReviewComponent: TextBlockRenderer,
  ImageMapComponent: TextBlockRenderer,
  PricingTableComponent: TextBlockRenderer,
};

const legacyNameRegistry: Record<string, React.ComponentType<CmsComponentProps>> = {
  Header: TextBlockRenderer,
  Footer: TextBlockRenderer,
  Banner: TextBlockRenderer,
  Image: TextBlockRenderer,
  "CTA Button": TextBlockRenderer,
  Paragraph: TextBlockRenderer,
  "Image Slider": TextBlockRenderer,
  Card: TextBlockRenderer,
  Testimonial: TextBlockRenderer,
  Gallery: TextBlockRenderer,
  "Pricing Table": TextBlockRenderer,
};

export const renderComponent = (
  component: ComponentDeliveryResponse,
  lang?: string,
): React.ReactElement => {
  const Renderer =
    registry[component.type] ??
    legacyNameRegistry[component.category] ??
    UnknownComponent;
  return React.createElement(Renderer, { component, lang });
};
