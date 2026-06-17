import Image from "next/image";
import Link from "next/link";
import { withLocalePath } from "@/lib/core/i18n/locale";
import type {
  PageResponse,
  ProductListDeliveryResponse,
} from "@/lib/commerce/product/types";

export type ProductListCopy = {
  searchLabel: string;
  searchPlaceholder: string;
  searchAction: string;
  clearAction: string;
  resultsLabel: string;
  emptyTitle: string;
  emptyDescription: string;
  productTypeFallback: string;
  priceLabel: string;
  detailsAction: string;
  previousAction: string;
  nextAction: string;
  pageLabel: string;
  imageAltFallback: string;
  errorTitle: string;
  errorDescription: string;
  retryAction: string;
};

type ProductListViewProps = {
  lang: string;
  query: string;
  pageNumber: number;
  products: PageResponse<ProductListDeliveryResponse> | null;
  errorMessage: string | null;
  copy: ProductListCopy;
};

const buildProductsHref = (
  lang: string,
  query: string,
  pageNumber: number,
): string => {
  const params = new URLSearchParams();
  if (query) {
    params.set("q", query);
  }
  if (pageNumber > 0) {
    params.set("page", String(pageNumber));
  }

  const queryString = params.toString();
  return `${withLocalePath(lang, "products")}${queryString ? `?${queryString}` : ""}`;
};

function ProductCard({
  lang,
  product,
  copy,
}: {
  lang: string;
  product: ProductListDeliveryResponse;
  copy: ProductListCopy;
}) {
  const detailHref = withLocalePath(lang, `products/${product.uid}`);

  return (
    <article className="product-card">
      <Link href={detailHref} className="product-card__media" aria-label={product.name}>
        {product.thumbnailUrl ? (
          <Image
            src={product.thumbnailUrl}
            alt={product.name || copy.imageAltFallback}
            fill
            sizes="(max-width: 760px) 100vw, (max-width: 1180px) 50vw, 33vw"
            className="product-card__image"
          />
        ) : (
          <span className="product-card__placeholder">{product.sku}</span>
        )}
      </Link>
      <div className="product-card__body">
        <div>
          <p className="product-card__type">
            {product.productTypeName || copy.productTypeFallback}
          </p>
          <h2 className="product-card__title">
            <Link href={detailHref}>{product.name}</Link>
          </h2>
          {product.shortDescription ? (
            <p className="product-card__description">{product.shortDescription}</p>
          ) : null}
        </div>
        <div className="product-card__footer">
          <div className="product-card__price">
            <span>{copy.priceLabel}</span>
            <strong>{product.price.formattedValue}</strong>
          </div>
          <Link href={detailHref} className="commerce-action commerce-action--secondary">
            {copy.detailsAction}
          </Link>
        </div>
      </div>
    </article>
  );
}

export function ProductListView({
  lang,
  query,
  pageNumber,
  products,
  errorMessage,
  copy,
}: ProductListViewProps) {
  const items = products?.content ?? [];
  const totalElements = products?.totalElements ?? 0;
  const totalPages = products?.totalPages ?? 0;
  const hasPrevious = Boolean(products && !products.first && pageNumber > 0);
  const hasNext = Boolean(products && !products.last);

  return (
    <section className="product-listing">
      <form className="product-search-panel surface-panel" action={withLocalePath(lang, "products")}>
        <label className="product-search-panel__label" htmlFor="product-search">
          {copy.searchLabel}
        </label>
        <div className="product-search-panel__controls">
          <input
            id="product-search"
            name="q"
            type="search"
            defaultValue={query}
            placeholder={copy.searchPlaceholder}
            className="product-search-panel__input"
          />
          <button type="submit" className="commerce-action">
            {copy.searchAction}
          </button>
          {query ? (
            <Link
              href={withLocalePath(lang, "products")}
              className="commerce-action commerce-action--secondary"
            >
              {copy.clearAction}
            </Link>
          ) : null}
        </div>
      </form>

      <div className="product-listing__meta">
        <p>
          {copy.resultsLabel}: {totalElements}
        </p>
        {totalPages > 0 ? (
          <p>
            {copy.pageLabel}: {pageNumber + 1} / {totalPages}
          </p>
        ) : null}
      </div>

      {errorMessage ? (
        <div className="surface-panel product-empty-state" role="alert">
          <h2 className="frame-title">{copy.errorTitle}</h2>
          <p className="frame-note">{errorMessage || copy.errorDescription}</p>
          <Link
            href={buildProductsHref(lang, query, pageNumber)}
            className="commerce-action commerce-action--secondary"
          >
            {copy.retryAction}
          </Link>
        </div>
      ) : items.length > 0 ? (
        <div className="product-grid">
          {items.map((product) => (
            <ProductCard
              key={product.uid}
              lang={lang}
              product={product}
              copy={copy}
            />
          ))}
        </div>
      ) : (
        <div className="surface-panel product-empty-state">
          <h2 className="frame-title">{copy.emptyTitle}</h2>
          <p className="frame-note">{copy.emptyDescription}</p>
        </div>
      )}

      {totalPages > 1 ? (
        <nav className="product-pagination" aria-label={copy.pageLabel}>
          {hasPrevious ? (
            <Link
              className="commerce-action commerce-action--secondary"
              href={buildProductsHref(lang, query, pageNumber - 1)}
            >
              {copy.previousAction}
            </Link>
          ) : (
            <span className="commerce-action-disabled">{copy.previousAction}</span>
          )}
          {hasNext ? (
            <Link
              className="commerce-action commerce-action--secondary"
              href={buildProductsHref(lang, query, pageNumber + 1)}
            >
              {copy.nextAction}
            </Link>
          ) : (
            <span className="commerce-action-disabled">{copy.nextAction}</span>
          )}
        </nav>
      ) : null}
    </section>
  );
}
