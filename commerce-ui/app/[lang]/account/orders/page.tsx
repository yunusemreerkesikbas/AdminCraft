import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";

export default async function OrdersPage() {
  const translate = await getTranslations("Orders");

  return (
    <PageShell
      eyebrow="orders"
      title={translate("title")}
      description={translate("description")}
    />
  );
}
