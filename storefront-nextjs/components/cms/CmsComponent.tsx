import { ComponentDeliveryResponse } from "@/lib/types";
import { renderComponent } from "./registry";

export default function CmsComponent({
  component,
  lang,
}: {
  component: ComponentDeliveryResponse;
  lang: string;
}): React.ReactElement | null {
  const rendered = renderComponent(component, lang);
  if (!rendered) return null;
  return <div id={component.uid}>{rendered}</div>;
}
