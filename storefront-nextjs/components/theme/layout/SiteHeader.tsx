"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";
import { usePathname } from "next/navigation";
import { useTranslations } from "next-intl";
import type {
  LayoutBlockDelivery,
  LayoutLinkDelivery,
  LanguageInfo,
  NavigationDeliveryResponse,
  NavigationSectionDelivery,
} from "@/lib/types";
import { toUrlLocale } from "@/lib/core/i18n/locale";
import styles from "./shell.module.css";

function CloseIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
      <path d="M1 1L13 13" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <path d="M13 1L1 13" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

function renderLink(
  link: LayoutLinkDelivery,
  className: string,
  key: string,
  onClick?: () => void
) {
  if (link.isExternal || link.target === "_blank") {
    return (
      <a
        key={key}
        href={link.href}
        className={className}
        target={link.target ?? "_blank"}
        rel="noopener noreferrer"
        onClick={onClick}
      >
        {link.label}
      </a>
    );
  }

  return (
    <Link key={key} href={link.href} className={className} onClick={onClick}>
      {link.label}
    </Link>
  );
}

function renderMenuSection(
  section: NavigationSectionDelivery,
  isOpen: boolean,
  onToggle: () => void,
  onClick?: () => void
) {
  if (section.links.length === 0 && section.href) {
    return renderLink(
      {
        uid: section.uid,
        label: section.title,
        href: section.href,
        isExternal: section.isExternal,
      },
      `${styles.panelNavLink} ${styles.panelNavLinkStandalone}`,
      section.uid,
      onClick,
    );
  }

  if (section.href) {
    return (
      <div key={section.uid} className={styles.panelNavSection}>
        <div className={styles.panelNavSectionHeader}>
          {renderLink(
            {
              uid: section.uid,
              label: section.title,
              href: section.href,
              isExternal: section.isExternal,
            },
            `${styles.panelNavLink} ${styles.panelNavLinkStandalone} ${styles.panelNavLinkWithToggle}`,
            section.uid,
            onClick,
          )}
          {section.links.length > 0 ? (
            <button
              type="button"
              className={`${styles.panelNavToggleButton} ${isOpen ? styles.panelNavToggleButtonOpen : ""}`}
              onClick={onToggle}
              aria-label={`${section.title} submenu`}
              aria-expanded={isOpen}
            >
              <span className={styles.panelNavToggle}>{isOpen ? "−" : "+"}</span>
            </button>
          ) : null}
        </div>
        <div className={`${styles.panelNavSubmenu} ${isOpen ? styles.panelNavSubmenuOpen : ""}`}>
          {section.links.map((link) => renderLink(link, styles.panelNavSubmenuLink, link.uid, onClick))}
        </div>
      </div>
    );
  }

  return (
    <div key={section.uid} className={styles.panelNavSection}>
      <button
        type="button"
        className={`${styles.panelNavLink} ${styles.panelNavTrigger} ${isOpen ? styles.panelNavTriggerOpen : ""}`}
        onClick={onToggle}
        aria-expanded={isOpen}
      >
        <span>{section.title}</span>
        <span className={styles.panelNavToggle}>{isOpen ? "−" : "+"}</span>
      </button>
      <div className={`${styles.panelNavSubmenu} ${isOpen ? styles.panelNavSubmenuOpen : ""}`}>
        {section.links.map((link) => renderLink(link, styles.panelNavSubmenuLink, link.uid, onClick))}
      </div>
    </div>
  );
}

function renderPrimaryInfoBlock(
  block: LayoutBlockDelivery,
  onClick?: () => void,
) {
  return (
    <div key={block.uid} {...smartEditBlockAttributes(block)}>
      {block.title ? <p className={styles.panelLabel}>{block.title}</p> : null}
      {block.links.map((link) => renderLink(link, styles.panelInfoLink, `${block.uid}-${link.uid}`, onClick))}
      {block.description ? <p className={styles.panelInfoText}>{block.description}</p> : null}
    </div>
  );
}

function renderSecondaryBlock(
  block: LayoutBlockDelivery,
  onClick?: () => void,
) {
  return (
    <div key={block.uid} {...smartEditBlockAttributes(block)}>
      {block.title ? <p className={styles.panelLabel}>{block.title}</p> : null}
      {block.links.length > 0 ? (
        <div className={styles.panelSocial}>
          {block.links.map((link) => renderLink(link, styles.panelSocialTextLink, `${block.uid}-${link.uid}`, onClick))}
        </div>
      ) : null}
      {block.description ? <p className={styles.panelInfoText}>{block.description}</p> : null}
    </div>
  );
}

function smartEditBlockAttributes(block: LayoutBlockDelivery | null | undefined) {
  if (!block) {
    return {};
  }

  return {
    "data-cms-component-id": block.uid,
    "data-cms-component-type": block.componentType,
    "data-cms-layout-role": block.role,
  };
}

export default function SiteHeader({
  lang,
  brand,
  mainNavigation,
  mainNavigationBlock,
  primaryBlocks,
  secondaryBlocks,
  enabledLanguages,
  logoUrl,
  logoDarkUrl,
}: {
  lang: string;
  brand: string;
  mainNavigation: (NavigationDeliveryResponse & { sections?: NavigationSectionDelivery[] }) | null;
  mainNavigationBlock?: LayoutBlockDelivery | null;
  primaryBlocks: LayoutBlockDelivery[];
  secondaryBlocks: LayoutBlockDelivery[];
  enabledLanguages: LanguageInfo[];
  logoUrl?: string;
  logoDarkUrl?: string;
}) {
  const t = useTranslations("ThemeChrome");
  const pathname = usePathname();
  const [isSticky, setIsSticky] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [openSectionUid, setOpenSectionUid] = useState<string | null>(null);

  const sections = mainNavigation?.sections ?? [];
  const homeHref = `/${lang}`;

  useEffect(() => {
    const onScroll = () => {
      setIsSticky(window.scrollY > 20);
    };

    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    document.body.style.overflow = menuOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [menuOpen]);

  useEffect(() => {
    if (!menuOpen) {
      return;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setMenuOpen(false);
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [menuOpen]);

  const switchPath = (targetLang: string): string => {
    const segments = pathname.split("/").filter(Boolean);
    if (segments.length === 0) {
      return `/${targetLang}`;
    }

    segments[0] = targetLang;
    return `/${segments.join("/")}`;
  };

  const toggleMenu = () => {
    setMenuOpen((current) => {
      const nextOpen = !current;
      if (nextOpen) {
        setOpenSectionUid((currentSectionUid) => currentSectionUid ?? sections[0]?.uid ?? null);
      }
      return nextOpen;
    });
  };

  return (
    <>
      <header className={`${styles.headerShell} ${isSticky ? styles.headerSticky : ""} ${menuOpen ? styles.headerHidden : ""}`}>
        <div className={styles.headerInner}>
          <div>
            <Link href={homeHref} className={styles.logoGroup} aria-label={brand}>
              <span className={`${styles.logoLink} ${styles.logoDark}`}>
                {logoUrl && (
                  <Image
                    src={logoUrl}
                    alt={brand}
                    fill
                    priority
                    sizes="85px"
                    className={styles.brandLogoImage}
                  />
                )}
              </span>
              <span className={`${styles.logoLink} ${styles.logoLight}`}>
                {logoDarkUrl && (
                  <Image
                    src={logoDarkUrl}
                    alt={brand}
                    fill
                    priority
                    sizes="85px"
                    className={styles.brandLogoImage}
                  />
                )}
              </span>
            </Link>
          </div>

          <div className={styles.menuColumn}>
            <button
              type="button"
              className={`${styles.menuButton} ${menuOpen ? styles.menuButtonOpen : ""}`}
              onClick={toggleMenu}
              aria-label="Open navigation"
              aria-expanded={menuOpen}
            >
              <span className={styles.menuButtonLine} />
              <span className={styles.menuButtonLine} />
            </button>
          </div>
        </div>
      </header>

      <div
        className={`${styles.panelBackdrop} ${menuOpen ? styles.panelBackdropVisible : ""}`}
        onClick={() => setMenuOpen(false)}
      />

      <div className={`${styles.offcanvasShell} ${menuOpen ? styles.offcanvasShellOpen : ""}`} aria-hidden={!menuOpen}>
        <div className={`${styles.offcanvasBg} ${styles.offcanvasBgLeft}`} />
        <div className={`${styles.offcanvasBg} ${styles.offcanvasBgRight}`} />

        <aside className={styles.offcanvasWrapper}>
          <div className={styles.offcanvasLeft}>
            <div className={styles.offcanvasLeftTop}>
              <Link href={homeHref} className={styles.offcanvasLogo} aria-label={brand} onClick={() => setMenuOpen(false)}>
                {(logoDarkUrl ?? logoUrl) && (
                  <Image
                    src={logoDarkUrl ?? logoUrl ?? ""}
                    alt={brand}
                    fill
                    priority
                    sizes="85px"
                    className={styles.brandLogoImage}
                  />
                )}
              </Link>

              <button
                type="button"
                className={`${styles.panelCloseButton} ${styles.panelCloseMobile}`}
                onClick={() => setMenuOpen(false)}
                aria-label="Close navigation"
              >
                <span className={styles.panelCloseLabel}>
                  <span>{t("closeLabel")}</span>
                </span>
                <span className={styles.panelCloseIcon}>
                  <CloseIcon />
                </span>
              </button>
            </div>

            <nav className={styles.panelNav} {...smartEditBlockAttributes(mainNavigationBlock)}>
              {sections.map((section) =>
                renderMenuSection(
                  section,
                  openSectionUid === section.uid,
                  () =>
                    setOpenSectionUid((current) =>
                      current === section.uid ? null : section.uid,
                    ),
                  () => setMenuOpen(false),
                )
              )}
            </nav>
          </div>

          <div className={styles.offcanvasRight}>
            <div className={styles.panelCloseRow}>
              <button
                type="button"
                className={styles.panelCloseButton}
                onClick={() => setMenuOpen(false)}
                aria-label="Close navigation"
              >
                <span className={styles.panelCloseLabel}>
                  <span>{t("closeLabel")}</span>
                </span>
                <span className={styles.panelCloseIcon}>
                  <CloseIcon />
                </span>
              </button>
            </div>

            <div className={styles.offcanvasRightInner}>
              <div className={styles.panelSideInfo}>
                {primaryBlocks.length > 0
                  ? primaryBlocks.map((block) => renderPrimaryInfoBlock(block, () => setMenuOpen(false)))
                  : (
                    <Link href={homeHref} className={styles.panelInfoLink} onClick={() => setMenuOpen(false)}>
                      {brand}
                    </Link>
                  )}
              </div>

              <div className={styles.panelMeta}>
                {secondaryBlocks.map((block) => renderSecondaryBlock(block, () => setMenuOpen(false)))}

                {enabledLanguages.length > 1 ? (
                  <div>
                    <p className={styles.panelLabel}>{t("language")}</p>
                    <div className={styles.panelLocales}>
                      {enabledLanguages.map((language) => {
                        const code = toUrlLocale(language.code);
                        const isActive = code === lang;
                        return (
                          <Link
                            key={language.code}
                            href={switchPath(code)}
                            className={`${styles.panelLocaleLink} ${isActive ? styles.panelLocaleActive : ""}`}
                            onClick={() => setMenuOpen(false)}
                          >
                            {language.nativeName}
                          </Link>
                        );
                      })}
                    </div>
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        </aside>
      </div>
    </>
  );
}
