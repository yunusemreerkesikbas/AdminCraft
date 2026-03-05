import CmsSlot from "@/components/cms/CmsSlot";
import { TEMPLATE_CONFIGS } from "./template-configs";
import { TemplateProps } from "./index";

export default function ErrorPageTemplate({ slotMap, lang }: TemplateProps) {
  const { slots } = TEMPLATE_CONFIGS.ErrorPageTemplate;
  return (
    <div className="cms-page ErrorPageTemplate">
      {slots.map((s) => (
        <section key={s.slotName} className={s.className}>
          <CmsSlot slotName={s.slotName} slotMap={slotMap} lang={lang} />
        </section>
      ))}
    </div>
  );
}
