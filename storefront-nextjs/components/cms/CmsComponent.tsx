import { ComponentDeliveryResponse } from "@/lib/types";
import { renderComponent } from "./registry";

export default function CmsComponent({
  component,
  lang,
}: {
  component: ComponentDeliveryResponse;
  lang?: string;
}) {
  return renderComponent(component, lang);
}
