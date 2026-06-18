import { getTranslations } from "next-intl/server";
import { createOrdersModel } from "@/components/orders/orders-model";
import { OrdersView } from "@/components/orders/OrdersView";
import {
  getCommerceBaseUrl,
  getTenantHeadersAsync,
} from "@/lib/core/config/runtime-env";

export default async function OrdersPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Orders");
  const account = await getTranslations("Account");
  const model = createOrdersModel(translate, account);

  return (
    <OrdersView
      model={model}
      apiBaseUrl={getCommerceBaseUrl()}
      lang={lang}
      tenantHeaders={await getTenantHeadersAsync()}
    />
  );
}
