import CmsSlot from "@/components/cms/CmsSlot";
import Motion from "@/components/theme/Motion";
import { TEMPLATE_CONFIGS } from "@/components/cms/templates/template-configs";
import type { TemplateProps } from "./index";

export default function LandingPageTemplate({ slotMap, lang }: TemplateProps) {
  const { slots } = TEMPLATE_CONFIGS.LandingPageTemplate;
  return (
    <div className="cms-page LandingPageTemplate">
      <Motion />
      {slots.map(({ slotName }) => (
        <CmsSlot key={slotName} slotName={slotName} slotMap={slotMap} lang={lang} />
      ))}
    </div>
  );
}
