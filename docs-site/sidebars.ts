import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  mainSidebar: [
    'intro',
    {
      type: 'category',
      label: 'Kavramlar',
      collapsed: false,
      items: ['concepts/platform-overview', 'concepts/content-model', 'concepts/glossary'],
    },
    {
      type: 'category',
      label: 'Geliştiriciler',
      collapsed: false,
      items: [
        'developers/module-flows',
        'developers/pagebuilder',
        'developers/smartedit',
        'developers/media',
        'developers/integrations',
      ],
    },
    {
      type: 'category',
      label: 'Editörler',
      collapsed: false,
      items: [
        'editors/pagebuilder-usage',
        'editors/smartedit-usage',
        'editors/media-management',
        'editors/publish-checklist',
        'editors/troubleshooting',
      ],
    },
    {
      type: 'category',
      label: 'Entegrasyon',
      collapsed: false,
      items: ['integrations/headless-delivery'],
    },
  ],
};

export default sidebars;
