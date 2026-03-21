import Image from "next/image";
import Link from "next/link";
import { buildMediaUrl } from "@/lib/core/media/url";
import type { EntryDeliveryResponse } from "@/lib/types";
import type { CmsComponentProps } from "./index";

type EntryRecord = EntryDeliveryResponse & Record<string, unknown>;

const readStringField = (entry: EntryRecord, key: string): string | undefined => {
  const direct = entry[key];
  if (typeof direct === "string" && direct.trim().length > 0) {
    return direct.trim();
  }

  const mapValue = entry.customFields?.[key];
  if (typeof mapValue === "string" && mapValue.trim().length > 0) {
    return mapValue.trim();
  }

  return undefined;
};

function PortfolioCard({ entry }: { entry: EntryRecord }) {
  const href = readStringField(entry, "linkUrl");
  const imageUrl = readStringField(entry, "imageUrl");
  const category = readStringField(entry, "category") ?? readStringField(entry, "badgeText");
  const title = entry.title ?? "";
  const description = entry.description;
  const altText = readStringField(entry, "altText") ?? title;
  const cardClasses = [
    "group overflow-hidden rounded-xl border border-slate-200 bg-white transition-colors duration-200",
    href ? "block cursor-pointer hover:border-slate-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2" : "",
  ]
    .filter(Boolean)
    .join(" ");

  const content = (
    <>
      {imageUrl && (
        <div className="relative aspect-[4/5] overflow-hidden bg-slate-100">
          <Image
            src={buildMediaUrl(imageUrl)}
            alt={altText}
            fill
            sizes="(max-width: 640px) 100vw, (max-width: 1280px) 50vw, 25vw"
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.02]"
          />
        </div>
      )}
      <div className="space-y-2 p-4">
        {category ? (
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">
            {category}
          </p>
        ) : null}
        <h3 className="text-lg font-semibold text-slate-900">{title}</h3>
        {description ? <p className="text-sm text-slate-600">{description}</p> : null}
      </div>
    </>
  );

  if (href && entry.isExternal) {
    return (
      <a
        href={href}
        target="_blank"
        rel="noopener noreferrer"
        className={cardClasses}
        aria-label={title}
      >
        {content}
      </a>
    );
  }

  if (!href) {
    return <article className={cardClasses}>{content}</article>;
  }

  return (
    <Link href={href} className={cardClasses} aria-label={title}>
      {content}
    </Link>
  );
}

export default function PortfolioGridRenderer({ component }: CmsComponentProps) {
  const entries = (component.entries ?? []) as EntryRecord[];
  if (entries.length === 0) {
    return null;
  }

  return (
    <section className="space-y-8">
      <div className="space-y-3">
        <h2 className="text-3xl font-semibold tracking-tight text-slate-950">
          {component.title}
        </h2>
        {component.subtitle ? (
          <p className="max-w-2xl text-base text-slate-600">{component.subtitle}</p>
        ) : null}
      </div>
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
        {entries.map((entry) => (
          <PortfolioCard key={entry.uid} entry={entry} />
        ))}
      </div>
    </section>
  );
}
