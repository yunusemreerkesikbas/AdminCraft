import type { PageTemplateConfig } from "@/lib/types";

const ContentPageTemplateConfig: PageTemplateConfig = {
  slots: [
    { slotName: "TopContent",  position: "TOP"    },
    { slotName: "BodyContent", position: "CENTER" },
    { slotName: "SideContent", position: "RIGHT"  },
  ],
};

export default ContentPageTemplateConfig;
