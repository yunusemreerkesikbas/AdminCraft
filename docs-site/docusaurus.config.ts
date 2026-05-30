import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const siteUrl = (process.env.DOCS_SITE_URL?.trim() || 'https://docs.craftive.io').replace(
  /\/+$/,
  '',
);
const craftiveWebsiteUrl =
  'https://craftive.io?utm_source=docs&utm_medium=referral&utm_campaign=docs_trust_signal';
const demoRequestUrl =
  'https://craftive.io?utm_source=docs&utm_medium=referral&utm_campaign=docs_trust_signal#contact';
const docsGa4MeasurementId = process.env.DOCS_GA4_MEASUREMENT_ID?.trim();
const googleSiteVerification = process.env.DOCS_GOOGLE_SITE_VERIFICATION?.trim();

const structuredData = [
  {
    '@context': 'https://schema.org',
    '@type': 'Organization',
    name: 'Craftive',
    url: 'https://craftive.io',
    logo: `${siteUrl}/img/craftive-logo-dark-text.svg`,
    sameAs: ['https://craftive.io'],
  },
  {
    '@context': 'https://schema.org',
    '@type': 'WebSite',
    name: 'Craftive Docs',
    url: siteUrl,
    inLanguage: ['tr', 'en'],
    publisher: {
      '@type': 'Organization',
      name: 'Craftive',
      url: 'https://craftive.io',
    },
  },
];

const config: Config = {
  title: 'Craftive Docs',
  tagline: 'Platform guides for developers and content teams',
  favicon: 'img/favicon.svg',
  url: siteUrl,
  baseUrl: '/',
  organizationName: 'craftive',
  projectName: 'craftive-docs',
  clientModules: ['./src/clientModules/outboundAnalytics.ts'],
  headTags: [
    ...(googleSiteVerification
      ? [
          {
            tagName: 'meta',
            attributes: {
              name: 'google-site-verification',
              content: googleSiteVerification,
            },
          },
        ]
      : []),
    {
      tagName: 'script',
      attributes: {
        type: 'application/ld+json',
      },
      innerHTML: JSON.stringify(structuredData),
    },
  ],

  onBrokenLinks: 'throw',
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },
  trailingSlash: false,

  i18n: {
    defaultLocale: 'tr',
    locales: ['tr', 'en'],
    localeConfigs: {
      tr: {
        label: 'Türkçe',
      },
      en: {
        label: 'English',
      },
    },
  },

  presets: [
    [
      'classic',
      {
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.ts',
          showLastUpdateAuthor: false,
          showLastUpdateTime: false,
        },
        blog: false,
        sitemap: {
          ignorePatterns: ['/search', '/en/search'],
        },
        ...(docsGa4MeasurementId
          ? {
              gtag: {
                trackingID: docsGa4MeasurementId,
                anonymizeIP: true,
              },
            }
          : {}),
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  plugins: [
    [
      require.resolve('@easyops-cn/docusaurus-search-local'),
      {
        hashed: true,
        language: ['tr', 'en'],
        indexDocs: true,
        indexBlog: false,
      },
    ],
  ],

  themeConfig: {
    image: 'img/social-card.svg',
    navbar: {
      logo: {
        alt: 'Craftive Docs',
        src: 'img/craftive-logo-dark-text.svg',
        srcDark: 'img/craftive-logo-light-text.svg',
      },
      items: [
        {type: 'docSidebar', sidebarId: 'mainSidebar', position: 'left', label: 'Rehberler'},
        {type: 'localeDropdown', position: 'right'},
        {href: craftiveWebsiteUrl, label: 'Craftive.io', position: 'right'},
      ],
    },
    footer: {
      style: 'light',
      links: [
        {
          title: 'Dokümanlar',
          items: [
            {label: 'Geliştiriciler', to: '/developers/module-flows'},
            {label: 'Editörler', to: '/editors/pagebuilder-usage'},
            {label: 'Kavramlar', to: '/concepts/platform-overview'},
          ],
        },
        {
          title: 'Craftive',
          items: [
            {label: 'Web sitesi', href: craftiveWebsiteUrl},
            {label: 'Demo talep et', href: demoRequestUrl},
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Craftive.`,
    },
    colorMode: {
      defaultMode: 'light',
      disableSwitch: false,
      respectPrefersColorScheme: false,
    },
    prism: {
      additionalLanguages: [],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
