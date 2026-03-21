import Link from "next/link";
import type { CmsComponentProps } from "@/components/cms/registry";

const isExternal = (href: string): boolean => /^https?:\/\//i.test(href);

export default function LinkRenderer({ component }: CmsComponentProps) {
  const links = (component.entries ?? []).filter((e) => e.isVisible !== false);
  if (links.length === 0) return null;

  return (
    <nav className="flex flex-wrap gap-3 p-4">
      {links.map((entry) => {
        const href = (entry.customFields?.linkUrl as string | undefined) ?? "";
        if (!href) return null;
        const label = entry.title ?? href;
        const external = isExternal(href);

        return external ? (
          <a
            key={entry.uid}
            href={href}
            target="_blank"
            rel="noopener noreferrer"
            className="text-sm font-medium text-slate-700 underline hover:text-slate-900"
          >
            {label}
          </a>
        ) : (
          <Link
            key={entry.uid}
            href={href}
            className="text-sm font-medium text-slate-700 underline hover:text-slate-900"
          >
            {label}
          </Link>
        );
      })}
    </nav>
  );
}
