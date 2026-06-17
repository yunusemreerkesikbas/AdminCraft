import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";
import { CommerceList } from "@/components/ui/StorefrontPrimitives";

export default async function OrdersPage() {
  const translate = await getTranslations("Orders");

  return (
    <PageShell
      eyebrow={translate("eyebrow")}
      title={translate("title")}
      description={translate("description")}
      visual={
        <CommerceList
          title={translate("listTitle")}
          items={[
            {
              title: translate("emptyTitle"),
              description: translate("emptyDescription"),
            },
          ]}
        />
      }
    />
  );
}
