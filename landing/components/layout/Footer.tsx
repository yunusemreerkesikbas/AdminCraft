import Link from "next/link";
import { Separator } from "@/components/ui/separator";
import siteConfig from "@/config/site.json";
import { Twitter, Github, Linkedin } from "lucide-react";

type FooterLink = { label: string; href: string };
type FooterGroup = { label: string; items: FooterLink[] };
type FooterLinks = { product: FooterGroup; company?: FooterGroup; resources: FooterGroup };
type FooterContent = {
  tagline: string;
  links: FooterLinks;
  copyright: string;
};

export function Footer({ content }: { content: FooterContent }) {
  const rawGroups: (FooterGroup | undefined)[] = content?.links
    ? [content.links.product, content.links.company, content.links.resources]
    : [];

  const groups: FooterGroup[] = rawGroups
    .filter((g): g is FooterGroup => Boolean(g && Array.isArray(g.items)))
    .filter((g) => g.items.length > 0);

  return (
    <footer className="border-t border-[var(--color-shade)] bg-[var(--color-light-neutral-1)]">
      <div className="mx-auto max-w-[1440px] px-4 py-14 sm:px-6 lg:px-20">
        <div className="grid gap-10 sm:grid-cols-2 lg:grid-cols-4">
          {/* Brand */}
          <div className="flex flex-col gap-4">
            <span className="font-heading text-xl font-semibold text-[var(--color-dark-neutral-1)]">
              {siteConfig.brandName}
            </span>
            <p className="text-sm leading-relaxed text-[var(--color-dark-neutral-2)]">
              {content.tagline}
            </p>
            <div className="flex gap-3">
              <a
                href={siteConfig.social.twitter}
                target="_blank"
                rel="noopener noreferrer"
                className="text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)] transition-colors"
                aria-label="Twitter"
              >
                <Twitter className="h-4 w-4" />
              </a>
              <a
                href={siteConfig.social.github}
                target="_blank"
                rel="noopener noreferrer"
                className="text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)] transition-colors"
                aria-label="GitHub"
              >
                <Github className="h-4 w-4" />
              </a>
              <a
                href={siteConfig.social.linkedin}
                target="_blank"
                rel="noopener noreferrer"
                className="text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)] transition-colors"
                aria-label="LinkedIn"
              >
                <Linkedin className="h-4 w-4" />
              </a>
            </div>
          </div>

          {/* Link groups */}
          {groups.map((group) => (
            <div key={group.label} className="flex flex-col gap-4">
              <span className="text-sm font-semibold text-[var(--color-dark-neutral-1)]">
                {group.label}
              </span>
              <ul className="flex flex-col gap-2">
                {group.items.map((item) => (
                  <li key={item.label}>
                    {item.href.startsWith("http") ? (
                      <a
                        href={item.href}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)] transition-colors"
                      >
                        {item.label}
                      </a>
                    ) : (
                      <Link
                        href={item.href}
                        className="text-sm text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)] transition-colors"
                      >
                        {item.label}
                      </Link>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <Separator className="my-10 bg-[var(--color-shade)]" />

        <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
          <p className="text-xs text-[var(--color-dark-neutral-2)]">{content.copyright}</p>
          <p className="text-xs text-[var(--color-dark-neutral-2)]">{siteConfig.domain}</p>
        </div>
      </div>
    </footer>
  );
}
