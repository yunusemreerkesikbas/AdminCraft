export type MediaModel = {
  src: string;
  alt: string;
  width: number;
  height: number;
};

export type ActionModel = {
  href: string;
  label: string;
  external?: boolean;
};

export type HeroModel = {
  title: string;
  subtitle?: string;
  description?: string;
  backgroundImage?: MediaModel | null;
  button?: ActionModel | null;
};

export type AboutModel = {
  title: string;
  eyebrow?: string;
  paragraphs: string[];
  mainImage?: MediaModel | null;
  innerImage?: MediaModel | null;
  innerLabel?: string;
  sideImage?: MediaModel | null;
};

export type VideoModel = {
  title: string;
  subtitle?: string;
  description?: string;
  videoUrl: string;
  posterImage?: MediaModel | null;
};

export type ServiceItemModel = {
  id: string;
  title: string;
  description?: string;
  icon?: MediaModel | null;
};

export type ServiceModel = {
  title: string;
  subtitle?: string;
  items: ServiceItemModel[];
};

export type ServiceHeroModel = {
  title: string;
  description?: string;
  backgroundImage?: MediaModel | null;
  overlayImage?: MediaModel | null;
};

export type ServicePanelItemModel = {
  id: string;
  panelNumber: number;
  panelSubtitle: string;
  title: string;
  description?: string;
  bullets: string[];
  image?: MediaModel | null;
  button?: ActionModel | null;
};

export type ServicePanelModel = {
  items: ServicePanelItemModel[];
};

export type ProjectItemModel = {
  id: string;
  title: string;
  subtitle?: string;
  href?: string;
  external?: boolean;
  image?: MediaModel | null;
};

export type AwardModel = {
  eyebrow?: string;
  primaryTitle: string;
  secondaryTitle?: string;
  description?: string;
  image?: MediaModel | null;
  button?: ActionModel | null;
};

export type MarqueeModel = {
  primary: string[];
  secondary: string[];
};

export type InstagramModel = {
  eyebrow?: string;
  handle: string;
  description?: string;
  mainImage?: MediaModel | null;
  floatingImages: MediaModel[];
  button?: ActionModel | null;
};

export type ContentHeroModel = {
  title: string;
  eyebrow?: string;
  description?: string;
  supportingText?: string;
  backgroundImage?: MediaModel | null;
  button?: ActionModel | null;
  scrollTarget?: string;
};

export type IntroListGroupModel = {
  id: string;
  title?: string;
  items: string[];
};

export type SplitMediaIntroModel = {
  heading: string;
  headingAccent?: string;
  introLabel?: string;
  paragraphs: string[];
  mainImage?: MediaModel | null;
  innerImage?: MediaModel | null;
  innerLabel?: string;
  sideImage?: MediaModel | null;
  listGroups: IntroListGroupModel[];
};

export type PersonCardModel = {
  id: string;
  name: string;
  role?: string;
  bio?: string;
  image?: MediaModel | null;
  profileHref?: string;
  external?: boolean;
};

export type PeopleCarouselModel = {
  title?: string;
  subtitle?: string;
  people: PersonCardModel[];
};

export type StatItemModel = {
  id: string;
  label: string;
  value: number;
  suffix?: string;
};

export type StatsGridModel = {
  title?: string;
  subtitle?: string;
  items: StatItemModel[];
};

export type LogoItemModel = {
  id: string;
  image: MediaModel;
  href?: string;
  external?: boolean;
};

export type LogoMarqueeModel = {
  title?: string;
  subtitle?: string;
  description?: string;
  logos: LogoItemModel[];
};

export type BrandGridItemModel = {
  id: string;
  image?: MediaModel | null;
  texts: string[];
};

export type BrandGridModel = {
  items: BrandGridItemModel[];
};

export type ImageMarqueeItemModel = {
  id: string;
  image: MediaModel;
};

export type ImageMarqueeModel = {
  items: ImageMarqueeItemModel[];
};

export type AwardShowcaseItemModel = {
  id: string;
  title: string;
  subtitle?: string;
  date?: string;
  image?: MediaModel | null;
};

export type AwardsShowcaseModel = {
  title: string;
  subtitle?: string;
  description?: string;
  items: AwardShowcaseItemModel[];
};

export type BigTextCtaModel = {
  leftLine: string;
  rightLine: string;
  button?: ActionModel | null;
};
