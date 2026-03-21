export const CMS_ENDPOINTS = {
  cmsPages: "cms/pages",
  cmsSite: "cms/site",
  cmsShell: "cms/shell",
  cmsMediaBatch: "cms/media",
  cmsProductByUid: "cms/products/${uid}",
  cmsProductsByCategory: "cms/products/category/${categoryUid}",
  cmsProductSearch: "cms/products/search",
  cmsSitemapPages: "cms/pages/sitemap",
  cmsRobotsTxt: "cms/robots.txt",
} as const;

export type CmsEndpointKey = keyof typeof CMS_ENDPOINTS;

export const resolveCmsEndpoint = (
  endpointKey: CmsEndpointKey,
  params: Record<string, string | number> = {},
): string => {
  const template = CMS_ENDPOINTS[endpointKey];

  return template.replace(/\$\{(\w+)\}/g, (match, key) => {
    const value = params[key];

    if (value === undefined || value === null) {
      throw new Error(`Parameter '${key}' is required but not provided`);
    }

    return encodeURIComponent(String(value));
  });
};
