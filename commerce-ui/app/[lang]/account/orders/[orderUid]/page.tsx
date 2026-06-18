import { getTranslations } from "next-intl/server";
import { OrderDetailView, type OrdersCopy } from "@/components/orders/OrdersView";
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
  const copy: OrdersCopy = {
    eyebrow: translate("eyebrow"),
    title: translate("title"),
    description: translate("description"),
    detailTitle: translate("detailTitle"),
    listTitle: translate("listTitle"),
    emptyTitle: translate("emptyTitle"),
    emptyDescription: translate("emptyDescription"),
    loadingLabel: translate("loadingLabel"),
    errorFallback: translate("errorFallback"),
    rowOrderUid: translate("rowOrderUid"),
    rowItems: translate("rowItems"),
    rowStatus: translate("rowStatus"),
    rowCreatedAt: translate("rowCreatedAt"),
    rowShipping: translate("rowShipping"),
    rowDelivery: translate("rowDelivery"),
    rowBilling: translate("rowBilling"),
    rowLegal: translate("rowLegal"),
    rowTotal: translate("rowTotal"),
    attentionLabel: translate("attentionLabel"),
    detailsAction: translate("detailsAction"),
    primaryAction: translate("primaryAction"),
    secondaryAction: translate("secondaryAction"),
    itemFallback: translate("itemFallback"),
    auth: {
      title: translate("authTitle"),
      description: translate("authDescription"),
      loginTab: account("loginTab"),
      registerTab: account("registerTab"),
      emailLabel: account("emailLabel"),
      passwordLabel: account("passwordLabel"),
      firstNameLabel: account("firstNameLabel"),
      lastNameLabel: account("lastNameLabel"),
      phoneLabel: account("phoneLabel"),
      rememberMeLabel: account("rememberMeLabel"),
      termsAcceptedLabel: account("termsAcceptedLabel"),
      privacyAcceptedLabel: account("privacyAcceptedLabel"),
      loginAction: account("loginAction"),
      registerAction: account("registerAction"),
      submittingLabel: account("submittingLabel"),
      errorFallback: account("errorFallback"),
    },
  };

  return (
    <OrderDetailView
      copy={copy}
      apiBaseUrl={getCommerceBaseUrl()}
      lang={lang}
      tenantHeaders={await getTenantHeadersAsync()}
      orderUid={orderUid}
    />
  );
}
