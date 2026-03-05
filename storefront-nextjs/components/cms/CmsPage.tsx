import { PageDeliveryResponse } from "@/lib/types";
import { buildSlotMap, resolveTemplate } from "./templates";
import { TEMPLATE_CONFIGS, TemplateName } from "./templates/template-configs";
import BodyClassSetter from "@/components/BodyClassSetter";
import CmsSlot from "./CmsSlot";

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
  const TemplateComponent = resolveTemplate(page.template);

  const chromeConfig = TEMPLATE_CONFIGS[page.template as TemplateName]?.chrome;
  const showHeader = chromeConfig?.header ?? true;
  const showFooter = chromeConfig?.footer ?? true;

  return (
    <>
      <BodyClassSetter pageUid={page.uid} />
      {showHeader && slotMap["Header"] && (
        <CmsSlot slotName="Header" slotMap={slotMap} lang={lang} />
      )}
      <main className="mx-auto w-full max-w-6xl px-6 py-10 space-y-10">
        <TemplateComponent slotMap={slotMap} page={page} lang={lang} />
        {children}
      </main>
      {showFooter && slotMap["Footer"] && (
        <CmsSlot slotName="Footer" slotMap={slotMap} lang={lang} />
      )}
    </>
  );
}
