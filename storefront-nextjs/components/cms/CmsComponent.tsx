import { ComponentDeliveryResponse } from "@/lib/types";
import { renderComponent } from "./registry";
import CmsErrorBoundary from "./CmsErrorBoundary";

export default function CmsComponent({
  component,
  lang,
}: {
  component: ComponentDeliveryResponse;
  lang: string;
}): React.ReactElement | null {
  const rendered = renderComponent(component, lang);
  if (!rendered) return null;
  return (
    <CmsErrorBoundary componentUid={component.uid}>
      <div id={component.uid}>{rendered}</div>
    </CmsErrorBoundary>
  );
}
