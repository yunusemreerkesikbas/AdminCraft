import CmsImage from "@/components/cms/CmsImage";
import type { CmsComponentProps } from "@/components/cms/registry";

export default function CarouselRenderer({ component }: CmsComponentProps) {
  const slides = (component.entries ?? []).filter((e) => e.isVisible !== false && e.responsive);
  if (slides.length === 0) return null;

  return (
    <section className="space-y-4 p-4">
      {component.title ? (
        <h2 className="text-2xl font-semibold text-slate-900">{component.title}</h2>
      ) : null}
      <div className="flex gap-4 overflow-x-auto pb-2">
        {slides.map((entry) => (
          <div key={entry.uid} className="shrink-0">
            <CmsImage
              media={entry.responsive}
              alt={entry.title ?? ""}
              className="rounded-lg object-cover"
            />
          </div>
        ))}
      </div>
    </section>
  );
}
