import Link from "next/link";
import { ComponentDeliveryResponse } from "@/lib/types";
import { fetchSiteConfig } from "@/lib/cms-client";
import { FALLBACK_LOCALE } from "@/lib/i18n";
import NavigationRenderer from "./NavigationRenderer";
import LanguageSwitcher from "@/components/layout/LanguageSwitcher";

export default async function HeaderCmsComponent({
  component,
  lang,
}: {
  component: ComponentDeliveryResponse;
  lang?: string;
}) {
  const currentLang = lang ?? FALLBACK_LOCALE;
  const site = await fetchSiteConfig();
  const brand = site?.siteTitle || site?.siteName || component.title;

  return (
    <header className="border-b border-slate-200/90 bg-white/95 backdrop-blur">
      <div className="mx-auto flex w-full max-w-6xl flex-wrap items-center justify-between gap-4 px-6 py-5">
        <Link href={`/${currentLang}`} className="text-lg font-semibold tracking-tight text-slate-950">
          {brand}
        </Link>

        <div className="flex flex-wrap items-center gap-4 sm:gap-6">
          {component.navigationNode ? (
            <NavigationRenderer node={component.navigationNode} lang={currentLang} />
          ) : null}

          {component.navigationLinkNode ? (
            <NavigationRenderer node={component.navigationLinkNode} lang={currentLang} />
          ) : null}

          {site && (
            <LanguageSwitcher enabledLanguages={site.enabledLanguages} currentLang={currentLang} />
          )}
        </div>
      </div>
    </header>
  );
}
