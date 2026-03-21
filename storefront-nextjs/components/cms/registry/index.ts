import React from "react";
import type { ComponentDeliveryResponse } from "@/lib/types";
import CMS_COMPONENTS_CONFIG from "@/components/cms/cms-components.config";
import UnknownComponent from "@/components/cms/renderers/UnknownComponent";

export type { CmsComponentProps } from "./types";

export const renderComponent = (
  component: ComponentDeliveryResponse,
  lang: string,
): React.ReactElement | null => {
  const Renderer = CMS_COMPONENTS_CONFIG[component.type] ?? UnknownComponent;
  return React.createElement(
    Renderer as React.ComponentType<{ component: ComponentDeliveryResponse; lang: string }>,
    { component, lang },
  );
};
