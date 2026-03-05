import { SlotMap } from "./templates";
import CmsComponent from "./CmsComponent";

interface CmsSlotProps {
  slotName: string;
  slotMap: SlotMap;
  lang?: string;
}

export default function CmsSlot({ slotName, slotMap, lang }: CmsSlotProps) {
  const slot = slotMap[slotName];
  if (!slot) {
    if (process.env.NODE_ENV === "development") {
      console.warn(`[CmsSlot] Slot "${slotName}" not found in slotMap`);
    }
    return null;
  }

  const components = slot.components?.component ?? [];

  return (
    <section className={`cms-slot ${slotName}`} data-slot-name={slotName}>
      {components.map((component) => (
        <CmsComponent key={component.uid} component={component} lang={lang} />
      ))}
    </section>
  );
}
