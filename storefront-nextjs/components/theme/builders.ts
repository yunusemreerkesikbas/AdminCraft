import type {
  CmsMediaDelivery,
  ComponentDeliveryResponse,
} from "@/lib/types";
import {
  readString,
  readEntryField,
  readEntryString,
  resolveLocalizedHref,
  toMediaModel,
  resolveMediaModel,
  type EntryRecord,
} from "@/lib/cms-utils";
import type {
  HeroModel,
  AboutModel,
  VideoModel,
  ServiceModel,
  ServiceHeroModel,
  ServicePanelModel,
  ProjectItemModel,
  AwardModel,
  MarqueeModel,
  InstagramModel,
  MediaModel,
  ContentHeroModel,
  SplitMediaIntroModel,
  PeopleCarouselModel,
  StatsGridModel,
  LogoMarqueeModel,
  BrandGridModel,
  AwardsShowcaseModel,
  BigTextCtaModel,
  ImageMarqueeModel,
} from "./models";

const readEntryMediaSource = (entry: EntryRecord | undefined): unknown =>
  readEntryField(entry, "responsive") ?? readEntryField(entry, "mediaUid");

const readComponentMediaSource = (component: ComponentDeliveryResponse | null): unknown =>
  component?.responsive;

const buildParagraphs = (value?: string): string[] =>
  (readString(value)?.split(/\r?\n+/).map((item) => item.trim()).filter(Boolean) ?? []);

const tokenizeMarquee = (value?: string): string[] =>
  (readString(value)?.split(/\s*-\s*/).map((item) => item.trim()).filter(Boolean) ?? []);

const readStringArray = (value: unknown): string[] => {
  if (Array.isArray(value)) {
    return value
      .map((item) => readString(item))
      .filter((item): item is string => Boolean(item));
  }

  const text = readString(value);
  return text ? text.split(/\r?\n+/).map((item) => item.trim()).filter(Boolean) : [];
};

const readNumericField = (entry: EntryRecord | undefined, key: string): number | null => {
  const raw = readEntryField(entry, key);
  const value = typeof raw === "number" ? raw : Number(raw);
  return Number.isFinite(value) ? value : null;
};

export const buildHeroModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): HeroModel | null => {
  if (!component) {
    return null;
  }

  const entry = component.entries?.[0] as EntryRecord | undefined;
  const title = readString(component.title) ?? "";
  const subtitle = readString(component.subtitle);
  const buttonLabel = readEntryString(entry, "buttonText");
  const href = resolveLocalizedHref(lang, readEntryString(entry, "buttonUrl"), entry?.isExternal ?? false);
  const button =
    buttonLabel && href
      ? { href, label: buttonLabel, external: entry?.isExternal ?? false }
      : null;

  return {
    title,
    subtitle,
    description: readString(component.description),
    backgroundImage: resolveMediaModel(
      mediaByUid,
      title,
      readEntryMediaSource(entry),
      readComponentMediaSource(component),
    ),
    button,
  };
};

export const buildServiceHeroModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): ServiceHeroModel | null => {
  if (!component) {
    return null;
  }

  const entry = component.entries?.[0] as EntryRecord | undefined;
  const title = readString(component.title) ?? "";

  if (!title) {
    return null;
  }

  return {
    title,
    description: readString(component.description),
    backgroundImage: resolveMediaModel(
      mediaByUid,
      title,
      readEntryMediaSource(entry),
      readComponentMediaSource(component),
    ),
    overlayImage: toMediaModel(
      readEntryField(entry, "overlayMediaUid"),
      mediaByUid,
      `${title} overlay`,
    ),
  };
};

export const buildAboutModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): AboutModel | null => {
  if (!component) {
    return null;
  }

  const entries = (component.entries ?? []) as EntryRecord[];

  return {
    title: readString(component.title) ?? "",
    eyebrow: readString(component.subtitle),
    paragraphs: buildParagraphs(component.description),
    mainImage: toMediaModel(readEntryMediaSource(entries[0]), mediaByUid, readString(entries[0]?.title) ?? ""),
    innerImage: toMediaModel(readEntryMediaSource(entries[1]), mediaByUid, readString(entries[1]?.title) ?? ""),
    innerLabel: readString(entries[1]?.title),
    sideImage: toMediaModel(readEntryMediaSource(entries[2]), mediaByUid, readString(entries[2]?.title) ?? ""),
  };
};

export const buildVideoModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): VideoModel | null => {
  if (!component) {
    return null;
  }

  const entry = component.entries?.[0] as EntryRecord | undefined;
  const videoUrl = readEntryString(entry, "videoUrl");

  if (!videoUrl) {
    return null;
  }

  return {
    title: readString(component.title) ?? "",
    subtitle: readString(component.subtitle),
    description: readString(component.description),
    videoUrl,
    posterImage: resolveMediaModel(
      mediaByUid,
      readString(component.title) ?? "",
      readEntryMediaSource(entry),
      readComponentMediaSource(component),
    ),
  };
};

export const buildServiceModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): ServiceModel | null => {
  if (!component) {
    return null;
  }

  const items = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => ({
      id: entry.uid,
      title: readString(entry.title) ?? "",
      description: readString(entry.description),
      icon: toMediaModel(readEntryMediaSource(entry), mediaByUid, readString(entry.title) ?? ""),
    }));

  if (items.length === 0) {
    return null;
  }

  return {
    title: readString(component.title) ?? "",
    subtitle: readString(component.subtitle),
    items,
  };
};

export const buildServicePanelModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): ServicePanelModel | null => {
  if (!component) {
    return null;
  }

  const items = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => {
      const panelNumber = readNumericField(entry, "panelId");
      const panelSubtitle = readEntryString(entry, "panelSubtitle") ?? "";
      const bullets = readStringArray(readEntryField(entry, "items"));
      const label = readEntryString(entry, "buttonText");
      const href = resolveLocalizedHref(lang, readEntryString(entry, "buttonUrl"), entry.isExternal);
      const button =
        label && href
          ? {
              href,
              label,
              external: entry.isExternal,
            }
          : null;

      return {
        id: entry.uid,
        panelNumber: panelNumber ?? 0,
        panelSubtitle,
        title: readString(entry.title) ?? "",
        description: readString(entry.description),
        bullets,
        image: toMediaModel(readEntryMediaSource(entry), mediaByUid, readString(entry.title) ?? ""),
        button,
      };
    })
    .filter((item) => item.title.length > 0 && item.panelSubtitle.length > 0);

  return items.length > 0 ? { items } : null;
};

export const buildProjectsModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): ProjectItemModel[] | null => {
  if (!component) {
    return null;
  }

  const componentMedia = readComponentMediaSource(component);
  const items = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => {
      const href = resolveLocalizedHref(lang, readEntryString(entry, "linkUrl"), entry.isExternal) ?? undefined;
      const entryMedia = readEntryMediaSource(entry);
      const mediaSource = entryMedia ?? componentMedia;
      return {
        id: entry.uid,
        title: readString(entry.title) ?? "",
        subtitle: readEntryString(entry, "subtitle"),
        href,
        external: entry.isExternal,
        image: toMediaModel(mediaSource, mediaByUid, readString(entry.title) ?? ""),
      };
    });

  return items.length > 0 ? items : null;
};

export const buildAwardModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): AwardModel | null => {
  if (!component) {
    return null;
  }

  const [primaryTitle = "", ...secondaryParts] = (readString(component.title) ?? "").split(/\s+/);
  const entry = component.entries?.[0] as EntryRecord | undefined;
  const buttonLabel = readEntryString(entry, "buttonText");
  const href = resolveLocalizedHref(lang, readEntryString(entry, "buttonUrl"), entry?.isExternal ?? false);
  const button =
    buttonLabel && href
      ? { href, label: buttonLabel, external: entry?.isExternal ?? false }
      : null;

  return {
    eyebrow: readString(component.subtitle),
    primaryTitle,
    secondaryTitle: secondaryParts.join(" "),
    description: readString(component.description),
    image: resolveMediaModel(
      mediaByUid,
      primaryTitle,
      readEntryMediaSource(entry),
      readComponentMediaSource(component),
    ),
    button,
  };
};

export const buildMarqueeModel = (component: ComponentDeliveryResponse | null): MarqueeModel | null => {
  if (!component) {
    return null;
  }

  const primary = tokenizeMarquee(component.description);
  if (primary.length === 0) {
    return null;
  }

  return {
    primary,
    secondary: [...primary].reverse(),
  };
};

export const buildInstagramModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): InstagramModel | null => {
  if (!component) {
    return null;
  }

  const visibleEntries = ((component.entries ?? []) as EntryRecord[]).filter((entry) => entry.isVisible !== false);
  const resolvedImages = visibleEntries
    .map((entry, index) =>
      toMediaModel(
        readEntryMediaSource(entry),
        mediaByUid,
        [readString(component.title), `${index + 1}`].filter(Boolean).join(" "),
      ),
    )
    .filter((image): image is MediaModel => Boolean(image));

  let mainImage: MediaModel | null = null;
  let floatingImages: MediaModel[] = [];

  if (resolvedImages.length >= 8) {
    mainImage = resolvedImages[resolvedImages.length - 1];
    floatingImages = resolvedImages.slice(0, 7);
  } else if (resolvedImages.length > 0) {
    [mainImage] = resolvedImages;
    floatingImages = resolvedImages.slice(1, 8);
  }

  if (!mainImage) {
    mainImage = toMediaModel(
      readComponentMediaSource(component),
      mediaByUid,
      readString(component.title) ?? "",
    );
  }

  const handle = readString(component.title) ?? "";
  const href = resolveLocalizedHref(lang, readEntryString(visibleEntries[0], "linkUrl"), visibleEntries[0]?.isExternal ?? false);
  const buttonLabel = readEntryString(visibleEntries[0], "buttonText");
  const button =
    buttonLabel && href
      ? { href, label: buttonLabel, external: visibleEntries[0]?.isExternal ?? false }
      : null;

  return {
    eyebrow: readString(component.subtitle),
    handle,
    description: readString(component.description),
    mainImage,
    floatingImages,
    button,
  };
};

export const buildContentHeroModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): ContentHeroModel | null => {
  if (!component) {
    return null;
  }

  const entry = component.entries?.[0] as EntryRecord | undefined;
  const title = readString(component.title) ?? "";
  const buttonLabel = readEntryString(entry, "buttonText");
  const href = resolveLocalizedHref(lang, readEntryString(entry, "buttonUrl"), entry?.isExternal ?? false);
  const button =
    buttonLabel && href
      ? { href, label: buttonLabel, external: entry?.isExternal ?? false }
      : null;

  return {
    title,
    eyebrow: readString(component.subtitle),
    description: readString(component.description),
    supportingText: readString(entry?.description) ?? readEntryString(entry, "supportingText"),
    backgroundImage: resolveMediaModel(
      mediaByUid,
      title,
      readEntryMediaSource(entry),
      readComponentMediaSource(component),
    ),
    button,
    scrollTarget: readEntryString(entry, "scrollTarget"),
  };
};

export const buildSplitMediaIntroModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): SplitMediaIntroModel | null => {
  if (!component) {
    return null;
  }

  const entries = (component.entries ?? []) as EntryRecord[];
  const listGroups = entries.slice(3)
    .map((entry) => ({
      id: entry.uid,
      title: readString(entry.title),
      items: readStringArray(readEntryField(entry, "items") ?? entry.description),
    }))
    .filter((group) => group.items.length > 0);

  return {
    heading: readString(component.title) ?? "",
    headingAccent: readString(component.subtitle),
    introLabel: readEntryString(entries[0], "introLabel"),
    paragraphs: buildParagraphs(component.description),
    mainImage: toMediaModel(readEntryMediaSource(entries[0]), mediaByUid, readString(entries[0]?.title) ?? ""),
    innerImage: toMediaModel(readEntryMediaSource(entries[1]), mediaByUid, readString(entries[1]?.title) ?? ""),
    innerLabel: readString(entries[1]?.title),
    sideImage: toMediaModel(readEntryMediaSource(entries[2]), mediaByUid, readString(entries[2]?.title) ?? ""),
    listGroups,
  };
};

export const buildPeopleCarouselModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): PeopleCarouselModel | null => {
  if (!component) {
    return null;
  }

  const people = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => ({
      id: entry.uid,
      name: readString(entry.title) ?? "",
      role: readEntryString(entry, "role"),
      bio: readString(entry.description),
      image: toMediaModel(readEntryMediaSource(entry), mediaByUid, readString(entry.title) ?? ""),
      profileHref: resolveLocalizedHref(lang, readEntryString(entry, "profileUrl") ?? readEntryString(entry, "linkUrl"), entry.isExternal) ?? undefined,
      external: entry.isExternal,
    }))
    .filter((person) => person.name.length > 0);

  return people.length > 0
    ? {
        title: readString(component.title),
        subtitle: readString(component.subtitle),
        people,
      }
    : null;
};

export const buildStatsGridModel = (
  component: ComponentDeliveryResponse | null,
): StatsGridModel | null => {
  if (!component) {
    return null;
  }

  const items = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => ({
      id: entry.uid,
      label: readString(entry.title) ?? "",
      value: readNumericField(entry, "statValue"),
      suffix: readEntryString(entry, "statSuffix"),
    }))
    .filter((item): item is { id: string; label: string; value: number; suffix: string | undefined } => item.label.length > 0 && item.value !== null);

  return items.length > 0
    ? {
        title: readString(component.title),
        subtitle: readString(component.subtitle),
        items,
      }
    : null;
};

export const buildLogoMarqueeModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): LogoMarqueeModel | null => {
  if (!component) {
    return null;
  }

  const logos = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => {
      const image = toMediaModel(readEntryMediaSource(entry), mediaByUid, readEntryString(entry, "altText") ?? readString(component.title) ?? "");
      if (!image) {
        return null;
      }

      return {
        id: entry.uid,
        image,
        href: resolveLocalizedHref(lang, readEntryString(entry, "linkUrl"), entry.isExternal) ?? undefined,
        external: entry.isExternal,
      };
    })
    .filter((logo): logo is { id: string; image: MediaModel; href: string | undefined; external: boolean } => Boolean(logo));

  return logos.length > 0 || readString(component.title) || readString(component.subtitle) || readString(component.description)
    ? {
        title: readString(component.title),
        subtitle: readString(component.subtitle),
        description: readString(component.description),
        logos,
      }
    : null;
};

export const buildBrandGridModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): BrandGridModel | null => {
  if (!component) {
    return null;
  }

  const items = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => {
      const texts = readStringArray(readEntryField(entry, "texts"));
      return {
        id: entry.uid,
        image: toMediaModel(readEntryMediaSource(entry), mediaByUid, readString(entry.title) ?? ""),
        texts,
      };
    })
    .filter((item) => Boolean(item.image) && item.texts.length > 0);

  return items.length > 0 ? { items } : null;
};

export const buildImageMarqueeModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): ImageMarqueeModel | null => {
  if (!component) {
    return null;
  }

  const items = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => {
      const image = toMediaModel(
        readEntryMediaSource(entry),
        mediaByUid,
        readEntryString(entry, "altText") ?? readString(component.title) ?? "",
      );

      return image
        ? {
            id: entry.uid,
            image,
          }
        : null;
    })
    .filter((item): item is NonNullable<typeof item> => Boolean(item));

  return items.length > 0 ? { items } : null;
};

export const buildAwardsShowcaseModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
): AwardsShowcaseModel | null => {
  if (!component) {
    return null;
  }

  const items = ((component.entries ?? []) as EntryRecord[])
    .filter((entry) => entry.isVisible !== false)
    .map((entry) => ({
      id: entry.uid,
      title: readString(entry.title) ?? "",
      subtitle: readEntryString(entry, "subtitle"),
      date: readEntryString(entry, "date"),
      image: toMediaModel(readEntryMediaSource(entry), mediaByUid, readString(entry.title) ?? ""),
    }))
    .filter((item) => item.title.length > 0);

  return items.length > 0
    ? {
        title: readString(component.title) ?? "",
        subtitle: readString(component.subtitle),
        description: readString(component.description),
        items,
      }
    : null;
};

export const buildBigTextCtaModel = (
  component: ComponentDeliveryResponse | null,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
): BigTextCtaModel | null => {
  if (!component) {
    return null;
  }

  const entry = component.entries?.[0] as EntryRecord | undefined;
  const leftLine = readString(component.title) ?? "";
  const rightLine = readString(component.subtitle) ?? "";
  const label = readEntryString(entry, "buttonText");
  const href = resolveLocalizedHref(lang, readEntryString(entry, "buttonUrl"), entry?.isExternal ?? false);
  const button =
    label && href
      ? {
          href,
          label,
          external: entry?.isExternal ?? false,
        }
      : null;

  if (!leftLine || !rightLine) {
    return null;
  }

  return {
    leftLine,
    rightLine,
    button,
  };
};
