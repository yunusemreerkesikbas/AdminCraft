import { getTranslations } from "next-intl/server";
import { CheckoutView, type CheckoutCopy } from "@/components/checkout/CheckoutView";
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
  const copy: CheckoutCopy = {
    eyebrow: translate("eyebrow"),
    title: translate("title"),
    description: translate("description"),
    authenticatedDescription: translate("authenticatedDescription"),
    secondaryAction: translate("secondaryAction"),
    summaryTitle: translate("summaryTitle"),
    summaryNote: translate("summaryNote"),
    rowStatus: translate("rowStatus"),
    rowShipping: translate("rowShipping"),
    rowSubtotal: translate("rowSubtotal"),
    rowVat: translate("rowVat"),
    rowShippingTotal: translate("rowShippingTotal"),
    shippingMethodStandardLabel: translate("shippingMethodStandardLabel"),
    shippingMethodFallback: translate("shippingMethodFallback"),
    totalLabel: translate("totalLabel"),
    addressStepTitle: translate("addressStepTitle"),
    addressStepDescription: translate("addressStepDescription"),
    deliveryAddressLabel: translate("deliveryAddressLabel"),
    billingSameAsDeliveryLabel: translate("billingSameAsDeliveryLabel"),
    billingAddressLabel: translate("billingAddressLabel"),
    noAddressOption: translate("noAddressOption"),
    startAction: translate("startAction"),
    updateAction: translate("updateAction"),
    loadingLabel: translate("loadingLabel"),
    errorFallback: translate("errorFallback"),
    validationTitle: translate("validationTitle"),
    validLabel: translate("validLabel"),
    invalidLabel: translate("invalidLabel"),
    cartChangedLabel: translate("cartChangedLabel"),
    priceChangedLabel: translate("priceChangedLabel"),
    stockChangedLabel: translate("stockChangedLabel"),
    warningKeysLabel: translate("warningKeysLabel"),
    paymentAction: translate("paymentAction"),
    paymentDisabled: translate("paymentDisabled"),
    paymentPreparingLabel: translate("paymentPreparingLabel"),
    itemSummaryTitle: translate("itemSummaryTitle"),
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
    },
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
    <CheckoutView
      copy={copy}
      apiBaseUrl={getCommerceBaseUrl()}
      lang={lang}
      tenantHeaders={await getTenantHeadersAsync()}
      cartHref={withLocalePath(lang, "cart")}
    />
  );
}
