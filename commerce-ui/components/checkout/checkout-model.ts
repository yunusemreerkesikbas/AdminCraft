import type { AddressBookModel } from "@/components/customer/address-book-model";
import { createAddressBookModel } from "@/components/customer/address-book-model";
import type {
  CustomerAuthModel,
  InlineCustomerAuthModel,
} from "@/components/customer/inline-customer-auth-model";
import { createCheckoutAuthModel } from "@/components/customer/inline-customer-auth-model";

export type CheckoutModel = {
  eyebrow: string;
  title: string;
  description: string;
  authenticatedDescription: string;
  secondaryAction: string;
  summaryTitle: string;
  summaryNote: string;
  rowStatus: string;
  rowShipping: string;
  rowSubtotal: string;
  rowVat: string;
  rowShippingTotal: string;
  shippingMethodStandardLabel: string;
  shippingMethodFallback: string;
  totalLabel: string;
  addressStepTitle: string;
  addressStepDescription: string;
  deliveryAddressLabel: string;
  billingSameAsDeliveryLabel: string;
  billingAddressLabel: string;
  noAddressOption: string;
  startAction: string;
  updateAction: string;
  loadingLabel: string;
  errorFallback: string;
  validationTitle: string;
  validLabel: string;
  invalidLabel: string;
  cartChangedLabel: string;
  priceChangedLabel: string;
  stockChangedLabel: string;
  warningKeysLabel: string;
  paymentAction: string;
  paymentDisabled: string;
  paymentPreparingLabel: string;
  itemSummaryTitle: string;
  itemFallback: string;
  legalTitle: string;
  legalDescription: string;
  legalReadyLabel: string;
  legalBlockedLabel: string;
  legalMissingLabel: string;
  legalAcceptLabel: string;
  legalVersionLabel: string;
  auth: CustomerAuthModel;
  addressBook: AddressBookModel;
};

type Translator<TModel> = (key: keyof TModel & string) => string;
type CheckoutTranslationModel = CheckoutModel & {
  authTitle: string;
  authDescription: string;
};

export const createCheckoutModel = (
  translate: Translator<CheckoutTranslationModel>,
  account: Translator<InlineCustomerAuthModel>,
  addressBook: Translator<AddressBookModel>,
): CheckoutModel => ({
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
  legalTitle: translate("legalTitle"),
  legalDescription: translate("legalDescription"),
  legalReadyLabel: translate("legalReadyLabel"),
  legalBlockedLabel: translate("legalBlockedLabel"),
  legalMissingLabel: translate("legalMissingLabel"),
  legalAcceptLabel: translate("legalAcceptLabel"),
  legalVersionLabel: translate("legalVersionLabel"),
  auth: createCheckoutAuthModel(translate, account),
  addressBook: createAddressBookModel(addressBook),
});
