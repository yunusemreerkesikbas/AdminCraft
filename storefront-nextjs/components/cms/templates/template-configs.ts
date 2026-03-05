import { PageTemplateConfig } from '@/lib/types';

const _configs = {
  LandingPageTemplate: {
    slots: [
      { slotName: 'Section1', position: 'TOP'    as const },
      { slotName: 'Section2', position: 'CENTER' as const },
      { slotName: 'Section3', position: 'BOTTOM' as const },
    ],
  },
  ContentPageTemplate: {
    slots: [
      { slotName: 'TopContent',  position: 'TOP' as const },
      { slotName: 'BodyContent', position: 'CENTER' as const },
      { slotName: 'SideContent', position: 'RIGHT' as const },
    ],
  },
  CategoryPageTemplate: {
    slots: [
      { slotName: 'TopContent',  position: 'TOP' as const },
      { slotName: 'ProductGrid', position: 'CENTER' as const },
    ],
  },
  ProductDetailsPageTemplate: {
    slots: [
      { slotName: 'Summary',      position: 'TOP' as const },
      { slotName: 'Tabs',         position: 'CENTER' as const },
      { slotName: 'CrossSelling', position: 'BOTTOM' as const },
    ],
  },
  SearchResultsPageTemplate: {
    slots: [
      { slotName: 'TopContent', position: 'TOP' as const },
      { slotName: 'Results',    position: 'CENTER' as const },
    ],
  },
  ErrorPageTemplate: {
    chrome: { header: false, footer: false },
    slots: [{ slotName: 'MiddleContent', position: 'CENTER' as const }],
  },
  NotFoundPageTemplate: {
    chrome: { header: false, footer: false },
    slots: [{ slotName: 'MiddleContent', position: 'CENTER' as const }],
  },
};

export type TemplateName = keyof typeof _configs;

export const TEMPLATE_CONFIGS: Record<TemplateName, PageTemplateConfig> = _configs;
