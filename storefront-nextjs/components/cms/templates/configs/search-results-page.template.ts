import type { PageTemplateConfig } from "@/lib/types";

const SearchResultsPageTemplateConfig: PageTemplateConfig = {
  slots: [
    { slotName: "TopContent", position: "TOP"    },
    { slotName: "Results",    position: "CENTER" },
  ],
};

export default SearchResultsPageTemplateConfig;
