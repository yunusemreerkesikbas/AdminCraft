import { getTranslations } from "next-intl/server";
import { createAccountModel } from "@/components/customer/account-model";
import { AccountView } from "@/components/customer/AccountView";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function AccountPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Account");
  const addressBook = await getTranslations("AddressBook");
  const model = createAccountModel(translate, addressBook);

  return (
    <AccountView
      model={model}
      ordersHref={withLocalePath(lang, "account/orders")}
    />
  );
}
