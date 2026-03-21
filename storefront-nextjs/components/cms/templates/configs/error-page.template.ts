import type { PageTemplateConfig } from "@/lib/types";

const ErrorPageTemplateConfig: PageTemplateConfig = {
  shell: { header: false, footer: false },
  slots: [{ slotName: "MiddleContent", position: "CENTER" }],
};

export default ErrorPageTemplateConfig;
