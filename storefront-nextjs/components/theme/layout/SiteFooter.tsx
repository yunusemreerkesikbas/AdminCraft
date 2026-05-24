import Image from "next/image";
import Link from "next/link";
import type { LayoutBlockDelivery, LayoutLinkDelivery } from "@/lib/types";
import styles from "./shell.module.css";
import NewsletterForm from "./NewsletterForm";

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
    <div
      key={block.uid}
      className={styles.footerColumnBrand}
      {...smartEditBlockAttributes(block)}
    >
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
    <div
      key={block.uid}
      className={styles.footerColumnOffice}
      {...smartEditBlockAttributes(block)}
    >
      {block.title ? <h4 className={styles.footerWidgetTitle}>{block.title}</h4> : null}
      {block.description ? <p className={styles.footerBrandText}>{block.description}</p> : null}
    </div>
  );
}

function renderNavigationBlock(block: LayoutBlockDelivery) {
  return (
    <div
      key={block.uid}
      className={styles.footerColumnSitemap}
      {...smartEditBlockAttributes(block)}
    >
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
    <div
      key={block.uid}
      className={styles.footerColumnNewsletter}
      {...smartEditBlockAttributes(block)}
    >
      {block.title ? <h4 className={styles.footerWidgetTitle}>{block.title}</h4> : null}
      <NewsletterForm
        placeholder={block.newsletterPlaceholder}
        buttonLabel={block.newsletterButtonLabel}
      />
    </div>
  );
}

function renderLinkListBlock(block: LayoutBlockDelivery) {
  return (
    <div
      key={block.uid}
      className={styles.footerColumnOffice}
      {...smartEditBlockAttributes(block)}
    >
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
      <div
        key={block.uid}
        className={styles.footerSocial}
        {...smartEditBlockAttributes(block)}
      >
        {block.links.map((link) => renderLink(link, styles.footerSocialLink, `${block.uid}-${link.uid}`))}
      </div>
    );
  }

  if (block.description) {
    return (
      <p
        key={block.uid}
        className={styles.footerCopyrightText}
        {...smartEditBlockAttributes(block)}
      >
        {block.description}
      </p>
    );
  }

  return null;
}

function smartEditBlockAttributes(block: LayoutBlockDelivery) {
  return {
    "data-cms-component-id": block.uid,
    "data-cms-component-type": block.componentType,
    "data-cms-layout-role": block.role,
  };
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
          {bottomBlocks.map((block) => renderBottomBlock(block))}
        </div>
      </div>
    </footer>
  );
}
