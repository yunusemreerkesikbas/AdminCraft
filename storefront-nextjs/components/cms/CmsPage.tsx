import { PageDeliveryResponse } from "@/lib/types";
import { buildSlotMap, renderTemplate } from "./templates";
import { TEMPLATE_CONFIGS, TemplateName } from "./templates/template-configs";
import BodyClassSetter from "@/components/BodyClassSetter";
import HeaderSlot from "@/components/layout/shell/HeaderSlot";
import FooterSlot from "@/components/layout/shell/FooterSlot";

export default async function CmsPage({
  page,
  lang,
  children,
}: {
  page: PageDeliveryResponse;
  lang: string;
  children?: React.ReactNode;
}) {
  const slotMap = buildSlotMap(page);

  const shellConfig = TEMPLATE_CONFIGS[page.template as TemplateName]?.shell;
  const showHeader = shellConfig?.header ?? true;
  const showFooter = shellConfig?.footer ?? true;
  const isLandingPage = page.template === "LandingPageTemplate";
  const isFullBleedPage =
    page.template === "LandingPageTemplate" || page.template === "ContentPageTemplate";

  return (
    <>
      <BodyClassSetter pageUid={page.uid} />
      {showHeader && (
        <HeaderSlot lang={lang} />
      )}
      <main
        className={
          isFullBleedPage
            ? "cms-main cms-main-landing"
            : "mx-auto w-full max-w-6xl px-6 py-10 space-y-10"
        }
      >
        {renderTemplate(page.template, { slotMap, page, lang })}
        {children}
      </main>
      {showFooter && (
        <FooterSlot lang={lang} lifted={isLandingPage} />
      )}
    </>
  );
}
