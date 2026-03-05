import { ComponentDeliveryResponse } from "@/lib/types";

export default function UnknownComponent({
  component,
}: {
  component: ComponentDeliveryResponse;
}) {
  return (
    <div className="rounded border border-dashed border-slate-300 p-4 text-sm text-slate-500">
      Unknown component: {component.type}
    </div>
  );
}
