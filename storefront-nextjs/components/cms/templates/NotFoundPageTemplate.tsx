import CmsSlot from "@/components/cms/CmsSlot";
import { TEMPLATE_CONFIGS } from "./template-configs";
import { TemplateProps } from "./index";

export default function NotFoundPageTemplate({ slotMap, lang }: TemplateProps) {
  const { slots } = TEMPLATE_CONFIGS.NotFoundPageTemplate;
  return (
    <div className="cms-page NotFoundPageTemplate">
      {slots.map((s) => (
        <section key={s.slotName} className={s.className}>
          <CmsSlot slotName={s.slotName} slotMap={slotMap} lang={lang} />
        </section>
      ))}
    </div>
  );
}
