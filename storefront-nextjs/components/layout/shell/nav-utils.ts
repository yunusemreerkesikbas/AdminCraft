import { NavigationEntryDeliveryResponse } from "@/lib/types";

export interface LayoutLinkModel {
  uid: string;
  label: string;
  href: string;
  isExternal: boolean;
  target?: string;
  color?: string;
}

export const resolveNavigationEntry = (
  entry: NavigationEntryDeliveryResponse
): LayoutLinkModel | null => {
  if (!entry.resolvedHref) return null;
  return {
    uid: entry.uid,
    label: entry.linkName ?? entry.url ?? entry.itemId ?? entry.uid,
    href: entry.resolvedHref,
    isExternal: entry.isExternal,
    target: entry.target,
    color: entry.linkColor,
  };
};
