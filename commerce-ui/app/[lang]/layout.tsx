import { NextIntlClientProvider } from "next-intl";
import { getMessages, setRequestLocale } from "next-intl/server";
import { notFound } from "next/navigation";
import { CommerceShell } from "@/components/ui/CommerceShell";
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

  return (
    <NextIntlClientProvider messages={messages}>
      <CommerceShell lang={messageLocale}>{children}</CommerceShell>
    </NextIntlClientProvider>
  );
}
