import CmsImage from "@/components/cms/CmsImage";
import type { CmsComponentProps } from "@/components/cms/registry";

export default function GenericBannerRenderer({ component }: CmsComponentProps) {
  return (
    <section className="space-y-4 p-6">
      {component.responsive ? (
        <CmsImage media={component.responsive} alt={component.title ?? ""} className="w-full rounded-lg object-cover" />
      ) : null}
      <div className="space-y-2">
        {component.title ? (
          <h2 className="text-2xl font-semibold text-slate-900">{component.title}</h2>
        ) : null}
        {component.subtitle ? (
          <p className="text-base font-medium text-slate-600">{component.subtitle}</p>
        ) : null}
        {component.description ? (
          <p className="text-sm text-slate-700">{component.description}</p>
        ) : null}
      </div>
    </section>
  );
}
