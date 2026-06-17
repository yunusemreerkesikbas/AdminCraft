import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  CommerceList,
} from "@/components/ui/StorefrontPrimitives";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function AccountPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Account");

  return (
    <PageShell
      eyebrow={translate("eyebrow")}
      title={translate("title")}
      description={translate("description")}
      actions={
        <ActionLink
          href={withLocalePath(lang, "account/orders")}
          label={translate("primaryAction")}
        />
      }
      visual={
        <CommerceList
          title={translate("listTitle")}
          items={[
            {
              title: translate("profileTitle"),
              description: translate("profileDescription"),
            },
            {
              title: translate("addressesTitle"),
              description: translate("addressesDescription"),
            },
            {
              title: translate("ordersTitle"),
              description: translate("ordersDescription"),
            },
          ]}
        />
      }
    />
  );
}
