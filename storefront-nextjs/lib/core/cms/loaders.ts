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

export const loadShellData = cache(async (lang: string) => {
  const [site, shell] = await Promise.all([getSiteConfig(lang), getShell(lang)]);

  return {
    site: requireSiteConfig(site),
    shell,
  };
});

export const loadHomepage = cache(async (lang: string) => {
  const [page, site] = await Promise.all([getCmsPage(lang), getSiteConfig(lang)]);

  return {
    page: requirePageOrNotFound(page),
    site: requireSiteConfig(site),
  };
});

export const loadContentPage = cache(async (lang: string, slugPath?: string) => {
  const [page, site] = await Promise.all([
    slugPath ? getCmsPage(lang, "ContentPage", slugPath) : getCmsPage(lang),
    getSiteConfig(lang),
  ]);

  return {
    page: requirePageOrNotFound(page),
    site: requireSiteConfig(site),
  };
});

export const loadProductPage = cache(async (lang: string, uid: string) => {
  const [page, site, product] = await Promise.all([
    getCmsPage(lang, "ProductPage", undefined, uid),
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
    getCmsPage(lang, "CategoryPage", undefined, categoryUid),
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
    getCmsPage(lang, "SearchResultPage"),
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
