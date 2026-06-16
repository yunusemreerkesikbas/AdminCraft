import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";

export default async function CartPage() {
  const translate = await getTranslations("Cart");

  return (
    <PageShell
      eyebrow="cart"
      title={translate("title")}
      description={translate("description")}
    />
  );
}
