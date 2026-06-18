import { getTranslations } from "next-intl/server";
import {
  CheckoutReturnView,
} from "@/components/checkout/CheckoutReturnView";
import { createCheckoutReturnModel } from "@/components/checkout/checkout-return-model";
import {
  getCommerceBaseUrl,
  getTenantHeadersAsync,
} from "@/lib/core/config/runtime-env";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function CheckoutReturnPage({
  params,
  searchParams,
}: {
  params: Promise<{ lang: string }>;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { lang } = await params;
  const query = await searchParams;
  const translate = await getTranslations("CheckoutReturn");
  const first = (key: string): string | null => {
    const value = query[key];
    if (Array.isArray(value)) {
      return value[0] ?? null;
    }
    return value ?? null;
  };
  const orderUid = first("orderUid");
  const model = createCheckoutReturnModel(translate);

  return (
    <CheckoutReturnView
      model={model}
      apiBaseUrl={getCommerceBaseUrl()}
      lang={lang}
      tenantHeaders={await getTenantHeadersAsync()}
      paymentStatus={first("paymentStatus")}
      attemptUid={first("attemptUid")}
      orderUid={orderUid}
      ordersHref={withLocalePath(lang, "account/orders")}
      orderHref={orderUid ? withLocalePath(lang, `account/orders/${orderUid}`) : null}
      checkoutHref={withLocalePath(lang, "checkout")}
      storeHref={withLocalePath(lang)}
    />
  );
}
