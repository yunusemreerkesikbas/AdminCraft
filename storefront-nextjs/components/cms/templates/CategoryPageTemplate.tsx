import CmsSlot from "@/components/cms/CmsSlot";
import { TEMPLATE_CONFIGS } from "./template-configs";
import { TemplateProps } from "./index";

export default function CategoryPageTemplate({ slotMap, lang }: TemplateProps) {
  const { slots } = TEMPLATE_CONFIGS.CategoryPageTemplate;
  return (
    <div className="cms-page CategoryPageTemplate">
      {slots.map((s) => (
        <section key={s.slotName} className={s.className}>
          <CmsSlot slotName={s.slotName} slotMap={slotMap} lang={lang} />
        </section>
      ))}
    </div>
  );
}
