import type {
  CmsMediaDelivery,
  ComponentDeliveryResponse,
  EntryDeliveryResponse,
} from "@/lib/types";
import type { MediaModel } from "@/components/theme/models";
import { withLocalePath } from "@/lib/core/i18n/locale";
import { buildMediaUrl } from "@/lib/core/media/url";

// ──────────────────────────────────────────────
// Shared utility helpers
// ──────────────────────────────────────────────

export type EntryRecord = EntryDeliveryResponse & Record<string, unknown>;

export const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

export const readString = (value: unknown): string | undefined =>
  typeof value === "string" && value.trim().length > 0 ? value.trim() : undefined;

export const readEntryField = (entry: EntryRecord | undefined, key: string): unknown => {
  if (!entry) {
    return undefined;
  }

  const direct = entry[key];
  if (direct !== undefined && direct !== null) {
    return direct;
  }

  const customFields = entry.customFields;
  if (isRecord(customFields)) {
    return customFields[key];
  }

  return undefined;
};

export const readEntryString = (entry: EntryRecord | undefined, key: string): string | undefined =>
  readString(readEntryField(entry, key));

export const resolveLocalizedHref = (
  lang: string,
  href: string | undefined,
  isExternal: boolean,
): string | null => {
  const normalizedHref = readString(href);
  if (!normalizedHref) return null;

  if (normalizedHref.startsWith("#")) return normalizedHref;
  if (/^(mailto:|tel:|ftp:|\/\/)/.test(normalizedHref)) return normalizedHref;
  if (isExternal) return normalizedHref;

  if (normalizedHref === "/") return `/${lang}`;
  if (normalizedHref.startsWith(`/${lang}/`) || normalizedHref === `/${lang}`) return normalizedHref;

  const path = normalizedHref.startsWith("/") ? normalizedHref : `/${normalizedHref}`;
  return withLocalePath(lang, path);
};

export const toMediaModel = (
  value: unknown,
  mediaByUid: Record<string, CmsMediaDelivery>,
  alt: string,
): MediaModel | null => {
  if (!value) {
    return null;
  }

  const mapMedia = (candidate: Partial<CmsMediaDelivery> | Record<string, unknown>): MediaModel | null => {
    const record = candidate as Record<string, unknown>;
    const desktopRecord = record.desktop as Record<string, unknown> | undefined;
    const publicUrl =
      readString(record.publicUrl) ??
      readString(record.url) ??
      readString(desktopRecord?.url) ??
      readString((record.mobile as Record<string, unknown> | undefined)?.url);

    if (!publicUrl) {
      return null;
    }

    const width = Number(record.width ?? desktopRecord?.width);
    const height = Number(record.height ?? desktopRecord?.height);
    if (!Number.isFinite(width) || width <= 0 || !Number.isFinite(height) || height <= 0) {
      return null;
    }

    return {
      src: buildMediaUrl(publicUrl),
      alt,
      width,
      height,
    };
  };

  if (typeof value === "string") {
    const trimmed = value.trim();
    if (!trimmed) {
      return null;
    }

    if (mediaByUid[trimmed]) {
      return mapMedia(mediaByUid[trimmed]);
    }

    return null;
  }

  if (isRecord(value)) {
    return mapMedia(value);
  }

  return null;
};

export const resolveMediaModel = (
  mediaByUid: Record<string, CmsMediaDelivery>,
  alt: string,
  ...sources: unknown[]
): MediaModel | null => {
  for (const source of sources) {
    const resolved = toMediaModel(source, mediaByUid, alt);
    if (resolved) {
      return resolved;
    }
  }

  return null;
};

export const collectStringMediaUids = (component?: ComponentDeliveryResponse | null): string[] => {
  if (!component) {
    return [];
  }

  return Array.from(
    new Set(
      (component.entries ?? [])
        .filter((entry) => !isRecord(readEntryField(entry as EntryRecord, "responsive")))
        .flatMap((entry) => [
          readEntryField(entry as EntryRecord, "mediaUid"),
          readEntryField(entry as EntryRecord, "overlayMediaUid"),
        ])
        .filter((value): value is string => typeof value === "string" && value.trim().length > 0),
    ),
  );
};
