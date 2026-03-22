import CmsImage from "@/components/cms/CmsImage";
import type { CmsComponentProps } from "@/components/cms/registry";

export default function ImageRenderer({ component }: CmsComponentProps) {
  if (!component.responsive) return null;

  return (
    <figure className="p-4">
      <CmsImage media={component.responsive} alt={component.title ?? ""} className="rounded-lg" />
      {component.title ? (
        <figcaption className="mt-2 text-center text-sm text-slate-500">{component.title}</figcaption>
      ) : null}
    </figure>
  );
}
