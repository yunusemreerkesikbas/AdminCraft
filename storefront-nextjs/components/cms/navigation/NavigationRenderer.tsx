import NavigationLinkItem from "./NavigationLinkItem";
import { NavigationDeliveryResponse, NavigationEntryDeliveryResponse } from "@/lib/types";
import { LayoutLinkModel, resolveNavigationEntry } from "@/components/layout/shell/nav-utils";

type ResolvedNavEntry = {
  entry: NavigationEntryDeliveryResponse;
  link: LayoutLinkModel;
};

const resolveEntries = (entries: NavigationEntryDeliveryResponse[]): ResolvedNavEntry[] =>
  entries.reduce<ResolvedNavEntry[]>((items, entry) => {
    const link = resolveNavigationEntry(entry);
    if (link) items.push({ entry, link });
    return items;
  }, []);

function NavNode({ node }: { node: NavigationDeliveryResponse }) {
  const hasChildren = node.children && node.children.length > 0;
  const links = resolveEntries(node.entries ?? []);
  const hasEntries = links.length > 0;

  return (
    <li className="relative group">
      {hasEntries && (
        <ul className="flex items-center gap-4">
          {links.map(({ entry, link }) => (
            <li key={entry.uid}>
              <NavigationLinkItem link={link} />
            </li>
          ))}
        </ul>
      )}
      {hasChildren && (
        <ul className="flex items-center gap-4">
          {node.children!.map((child) => (
            <NavNode key={child.uid} node={child} />
          ))}
        </ul>
      )}
    </li>
  );
}

export default function NavigationRenderer({
  node,
}: {
  node: NavigationDeliveryResponse;
  lang?: string;
}) {
  const hasChildren = node.children && node.children.length > 0;
  const links = resolveEntries(node.entries ?? []);
  const hasEntries = links.length > 0;

  if (!hasChildren && !hasEntries) return null;

  return (
    <nav className="flex items-center gap-4">
      <ul className="flex items-center gap-4 list-none m-0 p-0">
        {hasEntries &&
          links.map(({ entry, link }) => (
            <li key={entry.uid}>
              <NavigationLinkItem link={link} />
            </li>
          ))}
        {hasChildren &&
          node.children!.map((child) => (
            <NavNode key={child.uid} node={child} />
          ))}
      </ul>
    </nav>
  );
}
