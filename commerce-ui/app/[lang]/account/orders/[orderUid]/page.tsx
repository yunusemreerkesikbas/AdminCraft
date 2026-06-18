import { getTranslations } from "next-intl/server";
import { createOrdersModel } from "@/components/orders/orders-model";
import { OrderDetailView } from "@/components/orders/OrdersView";
import {
  getCommerceBaseUrl,
  getTenantHeadersAsync,
} from "@/lib/core/config/runtime-env";

export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ lang: string; orderUid: string }>;
}) {
  const { lang, orderUid } = await params;
  const translate = await getTranslations("Orders");
  const account = await getTranslations("Account");
  const model = createOrdersModel(translate, account);

  return (
    <OrderDetailView
      model={model}
      apiBaseUrl={getCommerceBaseUrl()}
      lang={lang}
      tenantHeaders={await getTenantHeadersAsync()}
      orderUid={orderUid}
    />
  );
}
