import { getTranslations } from "next-intl/server";
import { AccountView, type AccountCopy } from "@/components/customer/AccountView";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function AccountPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Account");
  const addressBook = await getTranslations("AddressBook");

  const copy: AccountCopy = {
    eyebrow: translate("eyebrow"),
    title: translate("title"),
    description: translate("description"),
    authenticatedDescription: translate("authenticatedDescription"),
    loginTab: translate("loginTab"),
    registerTab: translate("registerTab"),
    emailLabel: translate("emailLabel"),
    passwordLabel: translate("passwordLabel"),
    firstNameLabel: translate("firstNameLabel"),
    lastNameLabel: translate("lastNameLabel"),
    phoneLabel: translate("phoneLabel"),
    rememberMeLabel: translate("rememberMeLabel"),
    termsAcceptedLabel: translate("termsAcceptedLabel"),
    privacyAcceptedLabel: translate("privacyAcceptedLabel"),
    loginAction: translate("loginAction"),
    registerAction: translate("registerAction"),
    submittingLabel: translate("submittingLabel"),
    restoringLabel: translate("restoringLabel"),
    errorFallback: translate("errorFallback"),
    profileTitle: translate("profileTitle"),
    profileDescription: translate("profileDescription"),
    nameLabel: translate("nameLabel"),
    emailStatusLabel: translate("emailStatusLabel"),
    verifiedLabel: translate("verifiedLabel"),
    unverifiedLabel: translate("unverifiedLabel"),
    phoneValueFallback: translate("phoneValueFallback"),
    statusLabel: translate("statusLabel"),
    logoutAction: translate("logoutAction"),
    loggingOutLabel: translate("loggingOutLabel"),
    addressesTitle: translate("addressesTitle"),
    addressesDescription: translate("addressesDescription"),
    ordersTitle: translate("ordersTitle"),
    ordersDescription: translate("ordersDescription"),
    teaserLabel: translate("teaserLabel"),
    primaryAction: translate("primaryAction"),
    addressBook: {
      title: addressBook("title"),
      description: addressBook("description"),
      loadingLabel: addressBook("loadingLabel"),
      emptyTitle: addressBook("emptyTitle"),
      emptyDescription: addressBook("emptyDescription"),
      addAction: addressBook("addAction"),
      editAction: addressBook("editAction"),
      saveAction: addressBook("saveAction"),
      cancelAction: addressBook("cancelAction"),
      deleteAction: addressBook("deleteAction"),
      defaultDeliveryAction: addressBook("defaultDeliveryAction"),
      defaultBillingAction: addressBook("defaultBillingAction"),
      defaultDeliveryLabel: addressBook("defaultDeliveryLabel"),
      defaultBillingLabel: addressBook("defaultBillingLabel"),
      formTitleCreate: addressBook("formTitleCreate"),
      formTitleEdit: addressBook("formTitleEdit"),
      labelLabel: addressBook("labelLabel"),
      firstNameLabel: addressBook("firstNameLabel"),
      lastNameLabel: addressBook("lastNameLabel"),
      phoneLabel: addressBook("phoneLabel"),
      countryLabel: addressBook("countryLabel"),
      cityLabel: addressBook("cityLabel"),
      districtLabel: addressBook("districtLabel"),
      addressLine1Label: addressBook("addressLine1Label"),
      addressLine2Label: addressBook("addressLine2Label"),
      postalCodeLabel: addressBook("postalCodeLabel"),
      invoiceTypeLabel: addressBook("invoiceTypeLabel"),
      individualLabel: addressBook("individualLabel"),
      corporateLabel: addressBook("corporateLabel"),
      companyNameLabel: addressBook("companyNameLabel"),
      taxNumberLabel: addressBook("taxNumberLabel"),
      taxOfficeLabel: addressBook("taxOfficeLabel"),
      invoiceIdentityNumberLabel: addressBook("invoiceIdentityNumberLabel"),
      defaultDeliveryInputLabel: addressBook("defaultDeliveryInputLabel"),
      defaultBillingInputLabel: addressBook("defaultBillingInputLabel"),
      errorFallback: addressBook("errorFallback"),
      authRequiredLabel: addressBook("authRequiredLabel"),
    },
  };

  return (
    <AccountView
      copy={copy}
      ordersHref={withLocalePath(lang, "account/orders")}
    />
  );
}
