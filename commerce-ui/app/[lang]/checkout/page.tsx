import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";

export default async function CheckoutPage() {
  const translate = await getTranslations("Checkout");

  return (
    <PageShell
      eyebrow="checkout"
      title={translate("title")}
      description={translate("description")}
    />
  );
}
