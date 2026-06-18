import { getTranslations } from "next-intl/server";
import {
  CheckoutReturnView,
  type CheckoutReturnCopy,
} from "@/components/checkout/CheckoutReturnView";
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
  const copy: CheckoutReturnCopy = {
    eyebrow: translate("eyebrow"),
    title: translate("title"),
    successTitle: translate("successTitle"),
    failureTitle: translate("failureTitle"),
    unknownTitle: translate("unknownTitle"),
    successDescription: translate("successDescription"),
    failureDescription: translate("failureDescription"),
    unknownDescription: translate("unknownDescription"),
    loadingLabel: translate("loadingLabel"),
    summaryTitle: translate("summaryTitle"),
    summaryNote: translate("summaryNote"),
    rowStatus: translate("rowStatus"),
    rowAttempt: translate("rowAttempt"),
    rowOrder: translate("rowOrder"),
    rowProvider: translate("rowProvider"),
    rowTotal: translate("rowTotal"),
    missingValue: translate("missingValue"),
    primaryAction: translate("primaryAction"),
    orderAction: translate("orderAction"),
    retryAction: translate("retryAction"),
    secondaryAction: translate("secondaryAction"),
    errorFallback: translate("errorFallback"),
  };

  return (
    <CheckoutReturnView
      copy={copy}
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
