import NavigationLinkItem from "./NavigationLinkItem";
import { NavigationDeliveryResponse } from "@/lib/types";

export default function StaticNavRenderer({
  node,
}: {
  node: NavigationDeliveryResponse;
}) {
  const links = node.flatLinks ?? [];
  if (links.length === 0) return null;

  return (
    <ul className="flex flex-wrap items-center gap-4 list-none m-0 p-0">
      {links.map((link) => (
        <li key={link.uid}>
          <NavigationLinkItem link={link} />
        </li>
      ))}
    </ul>
  );
}
