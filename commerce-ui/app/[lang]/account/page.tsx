import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";

export default async function AccountPage() {
  const translate = await getTranslations("Account");

  return (
    <PageShell
      eyebrow="account"
      title={translate("title")}
      description={translate("description")}
    />
  );
}
