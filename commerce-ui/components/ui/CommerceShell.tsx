import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { withLocalePath, type BundledLocale } from "@/lib/core/i18n/locale";

type CommerceShellProps = {
  lang: BundledLocale;
  children: React.ReactNode;
};

export async function CommerceShell({ lang, children }: CommerceShellProps) {
  const navigation = await getTranslations("Navigation");
  const shell = await getTranslations("Shell");

  const links = [
    { href: withLocalePath(lang), label: navigation("home") },
    { href: withLocalePath(lang, "cart"), label: navigation("cart") },
    { href: withLocalePath(lang, "checkout"), label: navigation("checkout") },
    { href: withLocalePath(lang, "account"), label: navigation("account") },
    { href: withLocalePath(lang, "account/orders"), label: navigation("orders") },
  ];

  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b border-[var(--border)] bg-[var(--surface)]">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-6 py-5 md:flex-row md:items-center md:justify-between">
          <Link href={withLocalePath(lang)} className="space-y-1">
            <span className="block text-lg font-semibold">
              {shell("brand")}
            </span>
            <span className="block text-xs text-[var(--muted)]">
              {shell("subtitle")}
            </span>
          </Link>
          <nav className="flex flex-wrap gap-2 text-sm">
            {links.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="rounded-full border border-[var(--border)] px-3 py-1.5 text-[var(--muted)] transition hover:border-foreground hover:text-foreground"
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>
      </header>
      {children}
    </div>
  );
}
