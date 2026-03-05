import Link from "next/link";
import { NavigationDeliveryResponse, NavigationEntryDeliveryResponse } from "@/lib/types";

function NavEntry({ entry, lang }: { entry: NavigationEntryDeliveryResponse; lang: string }) {
  let href: string | undefined;
  let isExternal = false;

  if (entry.itemType === "URL") {
    href = entry.url;
    isExternal = entry.isExternal ?? false;
  } else if (entry.itemType === "PAGE") {
    href = `/${lang}/${entry.itemId}`;
  }

  if (!href) return null;

  const style = entry.linkColor ? { color: entry.linkColor } : undefined;
  const label = entry.linkName ?? entry.itemId ?? href;

  if (isExternal || entry.target === "_blank") {
    return (
      <a
        href={href}
        target="_blank"
        rel="noopener noreferrer"
        className="text-sm text-slate-600 transition-colors duration-200 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2"
        style={style}
      >
        {label}
      </a>
    );
  }

  return (
    <Link
      href={href}
      className="text-sm text-slate-600 transition-colors duration-200 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2"
      style={style}
    >
      {label}
    </Link>
  );
}

function NavNode({ node, lang }: { node: NavigationDeliveryResponse; lang: string }) {
  const hasChildren = node.children && node.children.length > 0;
  const hasEntries = node.entries && node.entries.length > 0;

  return (
    <li className="relative group">
      {hasEntries && (
        <ul className="flex items-center gap-4">
          {node.entries!.map((entry) => (
            <li key={entry.uid}>
              <NavEntry entry={entry} lang={lang} />
            </li>
          ))}
        </ul>
      )}
      {hasChildren && (
        <ul className="flex items-center gap-4">
          {node.children!.map((child) => (
            <NavNode key={child.uid} node={child} lang={lang} />
          ))}
        </ul>
      )}
    </li>
  );
}

export default function NavigationRenderer({
  node,
  lang,
}: {
  node: NavigationDeliveryResponse;
  lang: string;
}) {
  const hasChildren = node.children && node.children.length > 0;
  const hasEntries = node.entries && node.entries.length > 0;

  if (!hasChildren && !hasEntries) return null;

  return (
    <nav className="flex items-center gap-4">
      <ul className="flex items-center gap-4 list-none m-0 p-0">
        {hasEntries &&
          node.entries!.map((entry) => (
            <li key={entry.uid}>
              <NavEntry entry={entry} lang={lang} />
            </li>
          ))}
        {hasChildren &&
          node.children!.map((child) => (
            <NavNode key={child.uid} node={child} lang={lang} />
          ))}
      </ul>
    </nav>
  );
}
