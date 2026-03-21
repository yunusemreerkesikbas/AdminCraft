import { getMediaByUids } from "@/lib/core/cms/client";
import { collectStringMediaUids } from "@/lib/cms-utils";
import type { CmsComponentProps } from "@/components/cms/registry/types";
import type { CmsMediaDelivery, ComponentDeliveryResponse } from "@/lib/types";

type BuildFn<T> = (
  component: ComponentDeliveryResponse,
  mediaByUid: Record<string, CmsMediaDelivery>,
  lang: string,
) => T | null;

type RenderFn<T> = (model: T) => React.ReactElement | null | Promise<React.ReactElement | null>;

/**
 * Creates an async RSC renderer for a CMS component type.
 * - Collects mediaUids from component entries
 * - Fetches media (skips API call when no UIDs)
 * - Calls buildFn to produce a typed model
 * - Delegates rendering to renderFn (sync or async — supports inline dynamic imports)
 */
export function makeMediaRenderer<T>(
  buildFn: BuildFn<T>,
  renderFn: RenderFn<T>,
): (props: CmsComponentProps) => Promise<React.ReactElement | null> {
  return async function MediaRenderer({ component, lang }: CmsComponentProps) {
    const mediaUids = collectStringMediaUids(component);
    const mediaByUid = mediaUids.length > 0 ? await getMediaByUids(mediaUids) : {};
    const model = buildFn(component, mediaByUid, lang);
    if (model === null || model === undefined) return null;
    return await renderFn(model);
  };
}
