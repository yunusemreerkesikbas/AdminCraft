import React from "react";
import {
  ContentSlotDeliveryResponse,
  PageDeliveryResponse,
} from "@/lib/types";

export type SlotMap = Record<string, ContentSlotDeliveryResponse>;

export interface TemplateProps {
  slotMap: SlotMap;
  page: PageDeliveryResponse;
  lang?: string;
}

// Backend sets slotId = slotName + "Slot" (e.g. "Section1Slot").
// Templates reference slots by slotName (e.g. "Section1"), so we strip the "Slot" suffix.
const deriveSlotKey = (s: ContentSlotDeliveryResponse): string =>
  s.slotId.endsWith("Slot") ? s.slotId.slice(0, -4) : s.slotId;

export const buildSlotMap = (page: PageDeliveryResponse): SlotMap => {
  const slots = page.contentSlots?.contentSlot ?? [];
  return Object.fromEntries(slots.map((s) => [deriveSlotKey(s), s]));
};

import DefaultTemplate from "./DefaultTemplate";
import LandingPageTemplate from "./LandingPageTemplate";
import ContentPageTemplate from "./ContentPageTemplate";
import CategoryPageTemplate from "./CategoryPageTemplate";
import ProductDetailsPageTemplate from "./ProductDetailsPageTemplate";
import SearchResultsPageTemplate from "./SearchResultsPageTemplate";
import ErrorPageTemplate from "./ErrorPageTemplate";
import NotFoundPageTemplate from "./NotFoundPageTemplate";

const templateRegistry: Record<string, React.ComponentType<TemplateProps>> = {
  LandingPageTemplate,
  ContentPageTemplate,
  CategoryPageTemplate,
  ProductDetailsPageTemplate,
  SearchResultsPageTemplate,
  ErrorPageTemplate,
  NotFoundPageTemplate,
};

export const resolveTemplate = (
  templateName: string
): React.ComponentType<TemplateProps> => {
  if (process.env.NODE_ENV === "development" && !templateRegistry[templateName]) {
    console.warn(
      `[CmsPage] Unknown template: "${templateName}". Falling back to DefaultTemplate.`
    );
  }
  return templateRegistry[templateName] ?? DefaultTemplate;
};
