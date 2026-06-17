import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  ReceiptFrame,
} from "@/components/ui/StorefrontPrimitives";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ lang: string; orderUid: string }>;
}) {
  const { lang, orderUid } = await params;
  const translate = await getTranslations("Orders");

  return (
    <PageShell
      eyebrow={translate("eyebrow")}
      title={translate("detailTitle")}
      description={translate("description")}
      actions={
        <ActionLink
          href={withLocalePath(lang, "account/orders")}
          label={translate("primaryAction")}
          variant="secondary"
        />
      }
      visual={
        <ReceiptFrame
          title={translate("detailFrameTitle")}
          rows={[
            { label: translate("rowOrderUid"), value: orderUid },
            { label: translate("rowItems"), value: translate("rowItemsValue") },
            { label: translate("rowPayment"), value: translate("rowPaymentValue") },
            { label: translate("rowLegal"), value: translate("rowLegalValue") },
          ]}
        />
      }
    />
  );
}
