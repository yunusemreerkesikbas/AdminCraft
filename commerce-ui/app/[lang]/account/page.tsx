import { getTranslations } from "next-intl/server";
import { AccountView, type AccountCopy } from "@/components/customer/AccountView";

export default async function AccountPage() {
  const translate = await getTranslations("Account");

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
  };

  return <AccountView copy={copy} />;
}
