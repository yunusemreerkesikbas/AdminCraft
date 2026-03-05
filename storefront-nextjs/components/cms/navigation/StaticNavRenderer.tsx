import Link from "next/link";
import { NavigationDeliveryResponse, NavigationEntryDeliveryResponse } from "@/lib/types";

function StaticNavEntry({ entry, lang }: { entry: NavigationEntryDeliveryResponse; lang: string }) {
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
  const className =
    "text-sm text-slate-600 transition-colors duration-200 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2";

  if (isExternal || entry.target === "_blank") {
    return (
      <a href={href} target="_blank" rel="noopener noreferrer" className={className} style={style}>
        {label}
      </a>
    );
  }

  return (
    <Link href={href} className={className} style={style}>
      {label}
    </Link>
  );
}

export default function StaticNavRenderer({
  node,
  lang,
}: {
  node: NavigationDeliveryResponse;
  lang: string;
}) {
  const allEntries: NavigationEntryDeliveryResponse[] = [
    ...(node.entries ?? []),
    ...(node.children ?? []).flatMap((child) => child.entries ?? []),
  ];

  if (allEntries.length === 0) return null;

  return (
    <ul className="flex flex-wrap items-center gap-4 list-none m-0 p-0">
      {allEntries.map((entry) => (
        <li key={entry.uid}>
          <StaticNavEntry entry={entry} lang={lang} />
        </li>
      ))}
    </ul>
  );
}
