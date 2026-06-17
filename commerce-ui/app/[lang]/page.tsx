import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  CapabilityGrid,
  ProductFrame,
} from "@/components/ui/StorefrontPrimitives";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function HomePage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Home");
  const capabilities = [
    {
      title: translate("capabilityCatalogTitle"),
      description: translate("capabilityCatalogDescription"),
    },
    {
      title: translate("capabilityCartTitle"),
      description: translate("capabilityCartDescription"),
    },
    {
      title: translate("capabilityCheckoutTitle"),
      description: translate("capabilityCheckoutDescription"),
    },
    {
      title: translate("capabilityAccountTitle"),
      description: translate("capabilityAccountDescription"),
    },
  ];

  return (
    <PageShell
      eyebrow={translate("eyebrow")}
      title={translate("title")}
      description={translate("description")}
      actions={
        <>
          <ActionLink href={withLocalePath(lang, "cart")} label={translate("primaryAction")} />
          <ActionLink
            href={withLocalePath(lang, "account/orders")}
            label={translate("secondaryAction")}
            variant="secondary"
          />
        </>
      }
      visual={
        <ProductFrame
          label={translate("visualLabel")}
          status={[
            translate("visualStatusCatalog"),
            translate("visualStatusCheckout"),
            translate("visualStatusTenant"),
          ]}
        />
      }
    >
      <CapabilityGrid title={translate("capabilitiesLabel")} items={capabilities} />
    </PageShell>
  );
}
