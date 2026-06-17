import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { CartBadge } from "@/components/cart/CartBadge";
import { withLocalePath, type BundledLocale } from "@/lib/core/i18n/locale";

type CommerceShellProps = {
  lang: BundledLocale;
  children: React.ReactNode;
};

export async function CommerceShell({ lang, children }: CommerceShellProps) {
  const navigation = await getTranslations("Navigation");
  const shell = await getTranslations("Shell");

  const links = [
    { href: withLocalePath(lang, "products"), label: navigation("home") },
    { href: withLocalePath(lang, "checkout"), label: navigation("checkout") },
    { href: withLocalePath(lang, "account/orders"), label: navigation("orders") },
  ];

  return (
    <div className="commerce-shell">
      <a href="#main-content" className="skip-link">
        {shell("skipToContent")}
      </a>
      <header className="commerce-header">
        <div className="commerce-container commerce-header__inner">
          <Link
            href={withLocalePath(lang)}
            className="commerce-brand"
            translate="no"
          >
            <span className="commerce-brand__mark">{shell("brand")}</span>
            <span className="commerce-brand__subtitle">
              {shell("subtitle")}
            </span>
          </Link>
          <nav className="commerce-nav" aria-label={shell("primaryNavLabel")}>
            {links.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="commerce-nav__link"
              >
                {link.label}
              </Link>
            ))}
          </nav>
          <nav
            className="commerce-actions"
            aria-label={shell("utilityNavLabel")}
          >
            <Link
              href={withLocalePath(lang, "cart")}
              className="commerce-actions__link"
            >
              <CartBadge label={shell("cartAction")} />
            </Link>
            <Link
              href={withLocalePath(lang, "account")}
              className="commerce-actions__link"
            >
              {shell("accountAction")}
            </Link>
          </nav>
        </div>
      </header>
      {children}
    </div>
  );
}
