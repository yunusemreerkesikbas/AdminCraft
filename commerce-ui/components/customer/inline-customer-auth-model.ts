export type InlineCustomerAuthModel = {
  title: string;
  description: string;
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
  errorFallback: string;
};

export type CustomerAuthModel = Omit<InlineCustomerAuthModel, "errorFallback">;

type Translator = (key: string) => string;

const createCustomerAuthModel = (
  translate: Translator,
  account: Translator,
): CustomerAuthModel => ({
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
});

export const createInlineCustomerAuthModel = (
  translate: Translator,
  account: Translator,
): InlineCustomerAuthModel => ({
  ...createCustomerAuthModel(translate, account),
  errorFallback: account("errorFallback"),
});

export const createCheckoutAuthModel = (
  translate: Translator,
  account: Translator,
): CustomerAuthModel => createCustomerAuthModel(translate, account);
