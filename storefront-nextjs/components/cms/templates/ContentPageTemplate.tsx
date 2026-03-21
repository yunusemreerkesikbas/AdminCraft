import CmsSlot from "@/components/cms/CmsSlot";
import ContentPageMotion from "@/components/theme/ContentPageMotion";
import styles from "@/components/theme/content-page.module.css";
import serviceStyles from "@/components/theme/service-page.module.css";
import { SlotMap, TemplateProps } from "./index";

const hasVisibleComponents = (slotMap: SlotMap, slotName: string): boolean =>
  (slotMap[slotName]?.components?.component ?? []).some((component) => component.isVisible !== false);

const slotHasComponentType = (slotMap: SlotMap, slotName: string, componentType: string): boolean =>
  (slotMap[slotName]?.components?.component ?? []).some(
    (component) => component.isVisible !== false && component.type === componentType,
  );

export default function ContentPageTemplate({ slotMap, lang }: TemplateProps) {
  const hasTopContent = hasVisibleComponents(slotMap, "TopContent");
  const hasBodyContent = hasVisibleComponents(slotMap, "BodyContent");
  const hasSideContent = hasVisibleComponents(slotMap, "SideContent");
  const isServiceContentPage = slotHasComponentType(slotMap, "TopContent", "ServiceHeroComponent");

  return (
    <div
      className={[
        styles.pageShell,
        "cms-page ContentPageTemplate",
        isServiceContentPage ? serviceStyles.servicePageShell : "",
      ].filter(Boolean).join(" ")}
    >
      <ContentPageMotion />

      {hasTopContent ? (
        <div className={isServiceContentPage ? serviceStyles.serviceHeroRegion : styles.heroRegion}>
          <CmsSlot slotName="TopContent" slotMap={slotMap} lang={lang} />
        </div>
      ) : null}

      {hasBodyContent || hasSideContent ? (
        <div className={isServiceContentPage ? serviceStyles.serviceNarrativeRegion : styles.narrativeRegion}>
          {hasBodyContent ? (
            <div className={isServiceContentPage ? serviceStyles.serviceNarrativeColumn : styles.narrativeColumn}>
              <CmsSlot slotName="BodyContent" slotMap={slotMap} lang={lang} />
            </div>
          ) : null}

          {hasSideContent ? (
            <aside className={styles.sideRail}>
              <CmsSlot slotName="SideContent" slotMap={slotMap} lang={lang} />
            </aside>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
