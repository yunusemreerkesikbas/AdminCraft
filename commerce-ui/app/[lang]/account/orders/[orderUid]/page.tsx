import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";

export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ orderUid: string }>;
}) {
  const { orderUid } = await params;
  const translate = await getTranslations("Orders");

  return (
    <PageShell
      eyebrow={`${translate("uidLabel")}: ${orderUid}`}
      title={translate("detailTitle")}
      description={translate("description")}
    />
  );
}
