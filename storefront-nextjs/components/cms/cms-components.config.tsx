import dynamic from "next/dynamic";
import type { CmsComponentProps } from "./registry/types";
import { makeMediaRenderer } from "./renderers/renderer-factory";
import {
  buildHeroModel,
  buildAboutModel,
  buildVideoModel,
  buildAwardModel,
  buildServiceModel,
  buildServiceHeroModel,
  buildServicePanelModel,
  buildProjectsModel,
  buildInstagramModel,
  buildMarqueeModel,
  buildContentHeroModel,
  buildSplitMediaIntroModel,
  buildPeopleCarouselModel,
  buildStatsGridModel,
  buildLogoMarqueeModel,
  buildBrandGridModel,
  buildImageMarqueeModel,
  buildAwardsShowcaseModel,
  buildBigTextCtaModel,
} from "@/components/theme/builders";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type AnyRenderer = React.ComponentType<CmsComponentProps> | ((props: CmsComponentProps) => Promise<any>);

// ── Navigation ────────────────────────────────────────────────────────────────
const NavigationCmsComponent = dynamic(() => import("./navigation/NavigationCmsComponent"));

// ── Generic / fallback renderers ──────────────────────────────────────────────
const PortfolioGridRenderer = dynamic(() => import("./registry/PortfolioGridRenderer"));
const GenericBannerRenderer = dynamic(() => import("./renderers/GenericBannerRenderer"));
const ParagraphRenderer     = dynamic(() => import("./renderers/ParagraphRenderer"));
const LinkRenderer          = dynamic(() => import("./renderers/LinkRenderer"));
const ImageRenderer         = dynamic(() => import("./renderers/ImageRenderer"));
const CarouselRenderer      = dynamic(() => import("./renderers/CarouselRenderer"));

// ═══════════════════════════════════════════════════════════
// THEME PLUGIN POINT
// Bu dosya tema ile CMS pipeline'ı bağlar.
// Yeni tema için: builders ve sections klasörlerini değiştir.
// CMS infrastructure (cms/, layout/shell adapters, lib/) dokunulmaz.
// ═══════════════════════════════════════════════════════════

/**
 * Central CMS component registry — Spartacus SPA_CMSCOMPONENTS_CONFIG equivalent.
 *
 * Adding a new component type:
 *   1. Add build function to components/theme/builders.ts
 *   2. Register below with makeMediaRenderer(buildFn, async (m) => {
 *        const { default: MySection } = await import("@/components/theme/sections/MySection");
 *        return <MySection model={m} />;
 *      })
 */
const CMS_COMPONENTS_CONFIG: Record<string, AnyRenderer> = {
  // Navigation
  NavigationComponent: NavigationCmsComponent,

  // Landing page — type-specific (1:1 visual variant)
  // Spartacus pattern: inline async renderFn — section chunk only loaded when component is rendered
  HeroBannerComponent: makeMediaRenderer(buildHeroModel, async (m) => {
    const { default: HeroBanner } = await import("@/components/theme/sections/HeroBanner");
    return <HeroBanner model={m} />;
  }),
  AboutBannerComponent: makeMediaRenderer(buildAboutModel, async (m) => {
    const { default: AboutSection } = await import("@/components/theme/sections/AboutSection");
    return <AboutSection model={m} />;
  }),
  VideoSectionComponent: makeMediaRenderer(buildVideoModel, async (m) => {
    const { default: VideoSection } = await import("@/components/theme/sections/VideoSection");
    return <VideoSection model={m} />;
  }),
  AwardBannerComponent: makeMediaRenderer(buildAwardModel, async (m) => {
    const { default: AwardSection } = await import("@/components/theme/sections/AwardSection");
    return <AwardSection model={m} />;
  }),
  ServiceCardComponent: makeMediaRenderer(buildServiceModel, async (m) => {
    const { default: ServiceSection } = await import("@/components/theme/sections/ServiceSection");
    return <ServiceSection model={m} />;
  }),
  ProjectCardComponent: makeMediaRenderer(buildProjectsModel, async (p) => {
    const { default: ProjectSection } = await import("@/components/theme/sections/ProjectSection");
    return <ProjectSection projects={p} />;
  }),
  InstagramSectionComponent: makeMediaRenderer(buildInstagramModel, async (m) => {
    const { default: InstagramSection } = await import("@/components/theme/sections/InstagramSection");
    return <InstagramSection model={m} />;
  }),
  MarqueeTextComponent: makeMediaRenderer(buildMarqueeModel, async (m) => {
    const { default: MarqueeText } = await import("@/components/theme/sections/MarqueeText");
    return <MarqueeText model={m} />;
  }),
  ContentHeroComponent: makeMediaRenderer(buildContentHeroModel, async (m) => {
    const { default: ContentHeroSection } = await import("@/components/theme/sections/ContentHeroSection");
    return <ContentHeroSection model={m} />;
  }),
  ServiceHeroComponent: makeMediaRenderer(buildServiceHeroModel, async (m) => {
    const { default: ServiceHeroSection } = await import("@/components/theme/sections/ServiceHeroSection");
    return <ServiceHeroSection model={m} />;
  }),
  ServiceCardsGridComponent: makeMediaRenderer(buildServiceModel, async (m) => {
    const { default: ServiceCardsGridSection } = await import("@/components/theme/sections/ServiceCardsGridSection");
    return <ServiceCardsGridSection model={m} />;
  }),
  ServicePanelComponent: makeMediaRenderer(buildServicePanelModel, async (m) => {
    const { default: ServicePanelSection } = await import("@/components/theme/sections/ServicePanelSection");
    return <ServicePanelSection model={m} />;
  }),
  BrandGridComponent: makeMediaRenderer(buildBrandGridModel, async (m) => {
    const { default: BrandGridSection } = await import("@/components/theme/sections/BrandGridSection");
    return <BrandGridSection model={m} />;
  }),
  ImageMarqueeComponent: makeMediaRenderer(buildImageMarqueeModel, async (m) => {
    const { default: ImageMarqueeSection } = await import("@/components/theme/sections/ImageMarqueeSection");
    return <ImageMarqueeSection model={m} />;
  }),
  BigTextCtaComponent: makeMediaRenderer(buildBigTextCtaModel, async (m) => {
    const { default: BigTextCtaSection } = await import("@/components/theme/sections/BigTextCtaSection");
    return <BigTextCtaSection model={m} />;
  }),
  SplitMediaIntroComponent: makeMediaRenderer(buildSplitMediaIntroModel, async (m) => {
    const { default: SplitMediaIntroSection } = await import("@/components/theme/sections/SplitMediaIntroSection");
    return <SplitMediaIntroSection model={m} />;
  }),
  PeopleCarouselComponent: makeMediaRenderer(buildPeopleCarouselModel, async (m) => {
    const { default: PeopleCarouselSection } = await import("@/components/theme/sections/PeopleCarouselSection");
    return <PeopleCarouselSection model={m} />;
  }),
  StatsGridComponent: makeMediaRenderer(buildStatsGridModel, async (m) => {
    const { default: StatsGridSection } = await import("@/components/theme/sections/StatsGridSection");
    return <StatsGridSection model={m} />;
  }),
  LogoMarqueeComponent: makeMediaRenderer(buildLogoMarqueeModel, async (m) => {
    const { default: LogoMarqueeSection } = await import("@/components/theme/sections/LogoMarqueeSection");
    return <LogoMarqueeSection model={m} />;
  }),
  AwardsShowcaseComponent: makeMediaRenderer(buildAwardsShowcaseModel, async (m) => {
    const { default: AwardsShowcaseSection } = await import("@/components/theme/sections/AwardsShowcaseSection");
    return <AwardsShowcaseSection model={m} />;
  }),

  // Generic types (general-purpose fallback)
  SimpleBannerComponent:   GenericBannerRenderer,
  FeatureCardComponent:    PortfolioGridRenderer,
  CMSParagraphComponent:   ParagraphRenderer,
  CMSLinkComponent:        LinkRenderer,
  CMSImageComponent:       ImageRenderer,
  RotatingImagesComponent: CarouselRenderer,
};

export default CMS_COMPONENTS_CONFIG;
