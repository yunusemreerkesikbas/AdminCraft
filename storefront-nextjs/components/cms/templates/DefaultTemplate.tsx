import CmsSlot from "@/components/cms/CmsSlot";
import { TemplateProps } from "./index";

// Fallback: renders all slots from slotMap as a vertical stack
export default function DefaultTemplate({ slotMap, page, lang }: TemplateProps) {
  return (
    <div className={`cms-page ${page.template}`}>
      {Object.keys(slotMap).map((key) => (
        <section key={key}>
          <CmsSlot slotName={key} slotMap={slotMap} lang={lang} />
        </section>
      ))}
    </div>
  );
}
