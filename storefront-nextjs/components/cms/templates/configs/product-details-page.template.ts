import type { PageTemplateConfig } from "@/lib/types";

const ProductDetailsPageTemplateConfig: PageTemplateConfig = {
  slots: [
    { slotName: "Summary",      position: "TOP"    },
    { slotName: "Tabs",         position: "CENTER" },
    { slotName: "CrossSelling", position: "BOTTOM" },
  ],
};

export default ProductDetailsPageTemplateConfig;
