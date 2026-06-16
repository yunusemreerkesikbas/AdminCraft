import Link from "next/link";
import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function HomePage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Home");

  return (
    <PageShell
      eyebrow="commerce-ui"
      title={translate("title")}
      description={translate("description")}
    >
      <div className="flex flex-wrap gap-3">
        <Link
          href={withLocalePath(lang, "cart")}
          className="rounded-md bg-neutral-950 px-5 py-3 text-sm font-medium text-white"
        >
          {translate("primaryAction")}
        </Link>
        <Link
          href={withLocalePath(lang, "account/orders")}
          className="rounded-md border border-[var(--border)] px-5 py-3 text-sm font-medium"
        >
          {translate("secondaryAction")}
        </Link>
      </div>
    </PageShell>
  );
}
