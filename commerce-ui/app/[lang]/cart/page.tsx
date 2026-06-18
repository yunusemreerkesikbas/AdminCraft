import { getTranslations } from "next-intl/server";
import { CartPageView } from "@/components/cart/CartPageView";
import { createCartPageModel } from "@/components/cart/cart-page-model";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function CartPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Cart");
  const model = createCartPageModel(translate);

  return (
    <CartPageView
      model={model}
      lang={lang}
      storeHref={withLocalePath(lang)}
      checkoutHref={withLocalePath(lang, "checkout")}
    />
  );
}
