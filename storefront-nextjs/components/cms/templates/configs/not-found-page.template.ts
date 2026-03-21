import type { PageTemplateConfig } from "@/lib/types";

const NotFoundPageTemplateConfig: PageTemplateConfig = {
  shell: { header: false, footer: false },
  slots: [{ slotName: "MiddleContent", position: "CENTER" }],
};

export default NotFoundPageTemplateConfig;
