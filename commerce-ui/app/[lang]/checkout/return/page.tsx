import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";

export default async function CheckoutReturnPage() {
  const translate = await getTranslations("CheckoutReturn");

  return (
    <PageShell
      eyebrow="payment"
      title={translate("title")}
      description={translate("description")}
    />
  );
}
