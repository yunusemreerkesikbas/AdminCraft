import Link from "next/link";
import type { CSSProperties } from "react";
import type { LayoutLinkModel } from "@/components/layout/shell/nav-utils";

const DEFAULT_NAV_LINK_CLASS_NAME =
  "text-sm text-slate-600 transition-colors duration-200 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2";

export default function NavigationLinkItem({
  link,
  className = DEFAULT_NAV_LINK_CLASS_NAME,
}: {
  link: LayoutLinkModel;
  className?: string;
}) {
  const style: CSSProperties | undefined = link.color ? { color: link.color } : undefined;

  if (link.isExternal || link.target === "_blank") {
    return (
      <a
        href={link.href}
        target={link.target ?? "_blank"}
        rel="noopener noreferrer"
        className={className}
        style={style}
      >
        {link.label}
      </a>
    );
  }

  return (
    <Link href={link.href} className={className} style={style}>
      {link.label}
    </Link>
  );
}
