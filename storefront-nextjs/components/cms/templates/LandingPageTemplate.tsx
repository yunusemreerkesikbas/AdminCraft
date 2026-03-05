import CmsSlot from "@/components/cms/CmsSlot";
import { TEMPLATE_CONFIGS } from "./template-configs";
import { TemplateProps } from "./index";

export default function LandingPageTemplate({ slotMap, lang }: TemplateProps) {
  const { slots } = TEMPLATE_CONFIGS.LandingPageTemplate;
  return (
    <div className="cms-page LandingPageTemplate space-y-10">
      {slots.map((s) => (
        <section key={s.slotName} className={s.className}>
          <CmsSlot slotName={s.slotName} slotMap={slotMap} lang={lang} />
        </section>
      ))}
    </div>
  );
}
