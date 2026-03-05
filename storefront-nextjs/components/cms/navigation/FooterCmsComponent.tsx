import { ComponentDeliveryResponse } from "@/lib/types";
import { fetchSiteConfig } from "@/lib/cms-client";
import { FALLBACK_LOCALE } from "@/lib/i18n";
import NavigationRenderer from "./NavigationRenderer";

export default async function FooterCmsComponent({
  component,
  lang,
}: {
  component: ComponentDeliveryResponse;
  lang?: string;
}) {
  const site = await fetchSiteConfig();
  const currentLang = lang ?? FALLBACK_LOCALE;
  const brand = site?.siteTitle || site?.siteName || component.title;
  const year = new Date().getFullYear();

  return (
    <footer className="border-t border-slate-200 bg-white">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-4 px-6 py-8 text-sm text-slate-600 sm:flex-row sm:items-center sm:justify-between">
        <span className="text-sm font-medium text-slate-900">{`© ${year} ${brand}`}</span>
        {component.navigationNode && (
          <NavigationRenderer node={component.navigationNode} lang={currentLang} />
        )}
      </div>
    </footer>
  );
}
