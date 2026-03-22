import { NavigationType } from "@/lib/types";
import type { CmsComponentProps } from "@/components/cms/registry/index";
import NavigationRenderer from "./NavigationRenderer";
import StaticNavRenderer from "./StaticNavRenderer";
import SearchOverlay from "@/components/cms/search/SearchOverlay";

export default function NavigationCmsComponent({ component, lang }: CmsComponentProps) {
  if (!lang) {
    throw new Error("NavigationCmsComponent requires a locale.");
  }

  const navNode = component.navigationNode;
  const isStatic = component.navigationType === NavigationType.STATICPAGE;

  return (
    <nav className={`cms-navigation flex items-center gap-4 ${component.styleClasses ?? ""}`}>
      {navNode && isStatic ? (
        <StaticNavRenderer node={navNode} />
      ) : navNode ? (
        <NavigationRenderer node={navNode} lang={lang} />
      ) : null}
      {component.searchBox && <SearchOverlay lang={lang} />}
    </nav>
  );
}
