import type { AddressBookModel } from "./address-book-model";
import { createAddressBookModel } from "./address-book-model";

export type AccountModel = {
  eyebrow: string;
  title: string;
  description: string;
  authenticatedDescription: string;
  loginTab: string;
  registerTab: string;
  emailLabel: string;
  passwordLabel: string;
  firstNameLabel: string;
  lastNameLabel: string;
  phoneLabel: string;
  rememberMeLabel: string;
  termsAcceptedLabel: string;
  privacyAcceptedLabel: string;
  loginAction: string;
  registerAction: string;
  submittingLabel: string;
  restoringLabel: string;
  errorFallback: string;
  profileTitle: string;
  profileDescription: string;
  nameLabel: string;
  emailStatusLabel: string;
  verifiedLabel: string;
  unverifiedLabel: string;
  phoneValueFallback: string;
  statusLabel: string;
  logoutAction: string;
  loggingOutLabel: string;
  addressesTitle: string;
  addressesDescription: string;
  ordersTitle: string;
  ordersDescription: string;
  teaserLabel: string;
  primaryAction: string;
  addressBook: AddressBookModel;
};

type Translator<TModel> = (key: keyof TModel & string) => string;

export const createAccountModel = (
  translate: Translator<AccountModel>,
  addressBook: Translator<AddressBookModel>,
): AccountModel => ({
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
  addressBook: createAddressBookModel(addressBook),
});
