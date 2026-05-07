import { SlotMap } from "./templates";
import CmsComponent from "./CmsComponent";

interface CmsSlotProps {
  slotName: string;
  slotMap: SlotMap;
  lang: string;
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
    <div
      className={`cms-slot ${slotName}`}
      data-slot-name={slotName}
      data-cms-slot-id={slot.slotId}
      data-cms-slot-uuid={slot.slotUuid}
      data-cms-slot-position={slot.position}
      data-cms-slot-shared={slot.slotShared ? "true" : "false"}
    >
      {components.map((component) => (
        <CmsComponent key={component.uid} component={component} lang={lang} />
      ))}
    </div>
  );
}
