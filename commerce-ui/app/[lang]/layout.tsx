import { NextIntlClientProvider } from "next-intl";
import { getMessages, setRequestLocale } from "next-intl/server";
import { notFound } from "next/navigation";
import { CartProvider } from "@/components/cart/CartProvider";
import { CustomerSessionProvider } from "@/components/customer/CustomerSessionProvider";
import { CommerceShell } from "@/components/ui/CommerceShell";
import {
  getCommerceBaseUrl,
  getTenantHeadersAsync,
} from "@/lib/core/config/runtime-env";
import {
  isValidLocaleFormat,
  requireMessageLocale,
} from "@/lib/core/i18n/locale";

export default async function LocaleLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;

  if (!isValidLocaleFormat(lang)) {
    notFound();
  }

  let messageLocale: ReturnType<typeof requireMessageLocale>;
  try {
    messageLocale = requireMessageLocale(lang);
  } catch {
    notFound();
  }

  setRequestLocale(messageLocale);
  const messages = await getMessages();
  let tenantHeaders: Awaited<ReturnType<typeof getTenantHeadersAsync>>;
  let apiBaseUrl: string;
  try {
    tenantHeaders = await getTenantHeadersAsync();
    apiBaseUrl = getCommerceBaseUrl();
  } catch (error) {
    throw new Error("Failed to load commerce tenant configuration.", {
      cause: error,
    });
  }

  return (
    <NextIntlClientProvider messages={messages}>
      <CartProvider
        apiBaseUrl={apiBaseUrl}
        lang={messageLocale}
        tenantHeaders={tenantHeaders}
      >
        <CustomerSessionProvider
          apiBaseUrl={apiBaseUrl}
          lang={messageLocale}
          tenantHeaders={tenantHeaders}
        >
          <CommerceShell lang={messageLocale}>{children}</CommerceShell>
        </CustomerSessionProvider>
      </CartProvider>
    </NextIntlClientProvider>
  );
}
