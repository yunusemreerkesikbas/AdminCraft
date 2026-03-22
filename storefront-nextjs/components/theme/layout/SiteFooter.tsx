import Image from "next/image";
import Link from "next/link";
import type { LayoutBlockDelivery, LayoutLinkDelivery } from "@/lib/types";
import styles from "./shell.module.css";

function ArrowIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
      <path d="M3 9H15" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <path d="M10 4L15 9L10 14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

function renderLink(
  link: LayoutLinkDelivery,
  className: string,
  key: string,
) {
  if (link.isExternal || link.target === "_blank") {
    return (
      <a
        key={key}
        href={link.href}
        className={className}
        target={link.target ?? "_blank"}
        rel="noopener noreferrer"
      >
        {link.label}
      </a>
    );
  }

  return (
    <Link key={key} href={link.href} className={className}>
      {link.label}
    </Link>
  );
}

function renderParagraphBlock(
  block: LayoutBlockDelivery,
  lang: string,
  brand: string,
  logoUrl?: string,
) {
  return (
    <div key={block.uid} className={styles.footerColumnBrand}>
      <Link href={`/${lang}`} className={styles.footerLogo} aria-label={brand}>
        {logoUrl && (
          <Image
            src={logoUrl}
            alt={brand}
            fill
            sizes="85px"
            className={styles.brandLogoImage}
          />
        )}
      </Link>
      {block.title ? <h4 className={styles.footerWidgetTitle}>{block.title}</h4> : null}
      {block.description ? <p className={styles.footerBrandText}>{block.description}</p> : null}
    </div>
  );
}

function renderTextBlock(block: LayoutBlockDelivery) {
  return (
    <div key={block.uid} className={styles.footerColumnOffice}>
      {block.title ? <h4 className={styles.footerWidgetTitle}>{block.title}</h4> : null}
      {block.description ? <p className={styles.footerBrandText}>{block.description}</p> : null}
    </div>
  );
}

function renderNavigationBlock(block: LayoutBlockDelivery) {
  return (
    <div key={block.uid} className={styles.footerColumnSitemap}>
      {block.title ? <h4 className={styles.footerWidgetTitle}>{block.title}</h4> : null}
      <ul className={styles.footerMenu}>
        {block.links.map((link) => (
          <li key={link.uid} className={styles.footerMenuItem}>
            {renderLink(link, styles.footerMenuLink, `${block.uid}-${link.uid}`)}
          </li>
        ))}
      </ul>
    </div>
  );
}

function renderNewsletterBlock(block: LayoutBlockDelivery) {
  if (!block.newsletterPlaceholder || !block.newsletterButtonLabel) {
    return null;
  }

  return (
    <div key={block.uid} className={styles.footerColumnNewsletter}>
      {block.title ? <h4 className={styles.footerWidgetTitle}>{block.title}</h4> : null}
      <form onSubmit={(e) => e.preventDefault()}>
        <div className={styles.newsletterField}>
          <input
            type="email"
            placeholder={block.newsletterPlaceholder}
            aria-label={block.newsletterPlaceholder}
            className={styles.newsletterInput}
          />
          <button
            type="submit"
            className={styles.newsletterButton}
            aria-label={block.newsletterButtonLabel}
          >
            <ArrowIcon />
          </button>
        </div>
      </form>
    </div>
  );
}

function renderLinkListBlock(block: LayoutBlockDelivery) {
  return (
    <div key={block.uid} className={styles.footerColumnOffice}>
      {block.title ? <h4 className={styles.footerWidgetTitle}>{block.title}</h4> : null}
      <div className={styles.footerContactList}>
        {block.links.map((link) => renderLink(link, styles.footerContactLink, `${block.uid}-${link.uid}`))}
      </div>
      {block.description ? <p className={styles.footerBrandText}>{block.description}</p> : null}
    </div>
  );
}

function renderPrimaryBlock(
  block: LayoutBlockDelivery,
  lang: string,
  brand: string,
  logoUrl?: string,
) {
  if (block.role === "footer.brandBlock") {
    return renderParagraphBlock(block, lang, brand, logoUrl);
  }

  if (block.componentType === "CMSParagraphComponent") {
    return renderTextBlock(block);
  }

  if (block.componentType === "NavigationComponent") {
    return renderNavigationBlock(block);
  }

  if (block.newsletterPlaceholder && block.newsletterButtonLabel) {
    return renderNewsletterBlock(block);
  }

  return renderLinkListBlock(block);
}

function renderBottomBlock(block: LayoutBlockDelivery) {
  if (block.links.length > 0) {
    return (
      <div key={block.uid} className={styles.footerSocial}>
        {block.links.map((link) => renderLink(link, styles.footerSocialLink, `${block.uid}-${link.uid}`))}
      </div>
    );
  }

  if (block.description) {
    return <p key={block.uid} className={styles.footerCopyrightText}>{block.description}</p>;
  }

  return null;
}

export default async function SiteFooter({
  lang,
  brand,
  primaryBlocks,
  bottomBlocks,
  lifted = false,
  logoUrl,
}: {
  lang: string;
  brand: string;
  primaryBlocks: LayoutBlockDelivery[];
  bottomBlocks: LayoutBlockDelivery[];
  lifted?: boolean;
  logoUrl?: string;
}) {
  const year = new Date().getFullYear();

  return (
    <footer className={`${styles.footerShell} ${lifted ? styles.footerLifted : ""}`}>
      <div className={styles.footerPrimary}>
        <div className={styles.footerContainer}>
          <div className={styles.footerGrid}>
            {primaryBlocks.map((block) => renderPrimaryBlock(block, lang, brand, logoUrl))}
          </div>
        </div>
      </div>

      <div className={styles.footerCopyright}>
        <div className={styles.footerCopyrightInner}>
          <p className={styles.footerCopyrightText}>
            {`All rights reserved - ${year} © ${brand}`}
          </p>

          {bottomBlocks.map((block) => renderBottomBlock(block))}
        </div>
      </div>
    </footer>
  );
}
