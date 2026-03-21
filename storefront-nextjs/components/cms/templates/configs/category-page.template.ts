import type { PageTemplateConfig } from "@/lib/types";

const CategoryPageTemplateConfig: PageTemplateConfig = {
  slots: [
    { slotName: "TopContent",  position: "TOP"    },
    { slotName: "ProductGrid", position: "CENTER" },
  ],
};

export default CategoryPageTemplateConfig;
