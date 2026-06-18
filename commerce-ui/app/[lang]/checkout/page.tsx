import { getTranslations } from "next-intl/server";
import { createCheckoutModel } from "@/components/checkout/checkout-model";
import { CheckoutView } from "@/components/checkout/CheckoutView";
import {
  getCommerceBaseUrl,
  getTenantHeadersAsync,
} from "@/lib/core/config/runtime-env";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function CheckoutPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Checkout");
  const account = await getTranslations("Account");
  const addressBook = await getTranslations("AddressBook");
  const model = createCheckoutModel(translate, account, addressBook);

  return (
    <CheckoutView
      model={model}
      apiBaseUrl={getCommerceBaseUrl()}
      lang={lang}
      tenantHeaders={await getTenantHeadersAsync()}
      cartHref={withLocalePath(lang, "cart")}
    />
  );
}
