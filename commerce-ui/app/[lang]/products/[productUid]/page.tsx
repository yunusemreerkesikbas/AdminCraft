import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";

export default async function ProductPage({
  params,
}: {
  params: Promise<{ productUid: string }>;
}) {
  const { productUid } = await params;
  const translate = await getTranslations("Product");

  return (
    <PageShell
      eyebrow={`${translate("uidLabel")}: ${productUid}`}
      title={translate("title")}
      description={translate("description")}
    />
  );
}
