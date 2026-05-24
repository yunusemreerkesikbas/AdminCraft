import { cache } from "react";
import {
  getCategoryProducts,
  getCmsPage,
  getProduct,
  getShell,
  getSiteConfig,
  searchProducts,
} from "./client";
import { requireEntityOrNotFound, requirePageOrNotFound, requireSiteConfig } from "../errors/invariants";

export const loadSiteConfig = cache(async (lang?: string) =>
  requireSiteConfig(await getSiteConfig(lang)),
);

export const loadShellData = cache(async (lang: string, previewTicket?: string) => {
  const [site, shell] = await Promise.all([
    getSiteConfig(lang, previewTicket),
    getShell(lang, previewTicket),
  ]);

  return {
    site: requireSiteConfig(site),
    shell,
  };
});

export const loadHomepage = cache(async (lang: string, previewTicket?: string, previewPageId?: number) => {
  const [page, site] = await Promise.all([
    getCmsPage(lang, { previewTicket, previewPageId }),
    getSiteConfig(lang, previewTicket),
  ]);

  return {
    page: requirePageOrNotFound(page),
    site: requireSiteConfig(site),
  };
});

export const loadContentPage = cache(async (
  lang: string,
  slugPath?: string,
  previewTicket?: string,
  previewPageId?: number,
) => {
  const loadPage = async () => {
    if (!slugPath) {
      return getCmsPage(lang, { previewTicket, previewPageId });
    }

    const contentPage = await getCmsPage(lang, {
      pageType: "ContentPage",
      pageLabelOrId: slugPath,
      previewTicket,
      previewPageId,
    });

    if (contentPage) {
      return contentPage;
    }

    return getCmsPage(lang, {
      pageType: "LandingPage",
      pageLabelOrId: slugPath,
      previewTicket,
      previewPageId,
    });
  };

  const [page, site] = await Promise.all([
    loadPage(),
    getSiteConfig(lang, previewTicket),
  ]);

  return {
    page: requirePageOrNotFound(page),
    site: requireSiteConfig(site),
  };
});

export const loadProductPage = cache(async (lang: string, uid: string) => {
  const [page, site, product] = await Promise.all([
    getCmsPage(lang, { pageType: "ProductPage", code: uid }),
    getSiteConfig(lang),
    getProduct(uid, lang),
  ]);

  return {
    page: requirePageOrNotFound(page),
    site: requireSiteConfig(site),
    product: requireEntityOrNotFound(product),
  };
});

export const loadCategoryPage = cache(async (lang: string, categoryUid: string) => {
  const [page, site, products] = await Promise.all([
    getCmsPage(lang, { pageType: "CategoryPage", code: categoryUid }),
    getSiteConfig(lang),
    getCategoryProducts(categoryUid, lang),
  ]);

  return {
    page: requirePageOrNotFound(page),
    site: requireSiteConfig(site),
    products: requireEntityOrNotFound(products),
  };
});

export const loadSearchPage = cache(async (lang: string, query?: string) => {
  const normalizedQuery = query?.trim() ?? "";
  const [page, site, results] = await Promise.all([
    getCmsPage(lang, { pageType: "SearchResultPage" }),
    getSiteConfig(lang),
    normalizedQuery ? searchProducts(normalizedQuery, lang) : Promise.resolve(null),
  ]);

  return {
    page: requirePageOrNotFound(page),
    site: requireSiteConfig(site),
    query: normalizedQuery,
    results,
  };
});
