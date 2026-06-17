import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  ReceiptFrame,
} from "@/components/ui/StorefrontPrimitives";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function CheckoutReturnPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("CheckoutReturn");

  return (
    <PageShell
      eyebrow={translate("eyebrow")}
      title={translate("title")}
      description={translate("description")}
      actions={
        <>
          <ActionLink
            href={withLocalePath(lang, "account/orders")}
            label={translate("primaryAction")}
          />
          <ActionLink
            href={withLocalePath(lang)}
            label={translate("secondaryAction")}
            variant="secondary"
          />
        </>
      }
      visual={
        <ReceiptFrame
          title={translate("summaryTitle")}
          note={translate("summaryNote")}
          rows={[
            { label: translate("rowAttempt"), value: translate("rowAttemptValue") },
            { label: translate("rowOrder"), value: translate("rowOrderValue") },
            { label: translate("rowRedirect"), value: translate("rowRedirectValue") },
          ]}
        />
      }
    />
  );
}
