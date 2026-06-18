export type AddressBookModel = {
  title: string;
  description: string;
  loadingLabel: string;
  emptyTitle: string;
  emptyDescription: string;
  addAction: string;
  editAction: string;
  saveAction: string;
  cancelAction: string;
  deleteAction: string;
  defaultDeliveryAction: string;
  defaultBillingAction: string;
  defaultDeliveryLabel: string;
  defaultBillingLabel: string;
  formTitleCreate: string;
  formTitleEdit: string;
  labelLabel: string;
  firstNameLabel: string;
  lastNameLabel: string;
  phoneLabel: string;
  countryLabel: string;
  cityLabel: string;
  districtLabel: string;
  addressLine1Label: string;
  addressLine2Label: string;
  postalCodeLabel: string;
  invoiceTypeLabel: string;
  individualLabel: string;
  corporateLabel: string;
  companyNameLabel: string;
  taxNumberLabel: string;
  taxOfficeLabel: string;
  invoiceIdentityNumberLabel: string;
  defaultDeliveryInputLabel: string;
  defaultBillingInputLabel: string;
  errorFallback: string;
  authRequiredLabel: string;
};

type Translator = (key: string) => string;

export const createAddressBookModel = (
  translate: Translator,
): AddressBookModel => ({
  title: translate("title"),
  description: translate("description"),
  loadingLabel: translate("loadingLabel"),
  emptyTitle: translate("emptyTitle"),
  emptyDescription: translate("emptyDescription"),
  addAction: translate("addAction"),
  editAction: translate("editAction"),
  saveAction: translate("saveAction"),
  cancelAction: translate("cancelAction"),
  deleteAction: translate("deleteAction"),
  defaultDeliveryAction: translate("defaultDeliveryAction"),
  defaultBillingAction: translate("defaultBillingAction"),
  defaultDeliveryLabel: translate("defaultDeliveryLabel"),
  defaultBillingLabel: translate("defaultBillingLabel"),
  formTitleCreate: translate("formTitleCreate"),
  formTitleEdit: translate("formTitleEdit"),
  labelLabel: translate("labelLabel"),
  firstNameLabel: translate("firstNameLabel"),
  lastNameLabel: translate("lastNameLabel"),
  phoneLabel: translate("phoneLabel"),
  countryLabel: translate("countryLabel"),
  cityLabel: translate("cityLabel"),
  districtLabel: translate("districtLabel"),
  addressLine1Label: translate("addressLine1Label"),
  addressLine2Label: translate("addressLine2Label"),
  postalCodeLabel: translate("postalCodeLabel"),
  invoiceTypeLabel: translate("invoiceTypeLabel"),
  individualLabel: translate("individualLabel"),
  corporateLabel: translate("corporateLabel"),
  companyNameLabel: translate("companyNameLabel"),
  taxNumberLabel: translate("taxNumberLabel"),
  taxOfficeLabel: translate("taxOfficeLabel"),
  invoiceIdentityNumberLabel: translate("invoiceIdentityNumberLabel"),
  defaultDeliveryInputLabel: translate("defaultDeliveryInputLabel"),
  defaultBillingInputLabel: translate("defaultBillingInputLabel"),
  errorFallback: translate("errorFallback"),
  authRequiredLabel: translate("authRequiredLabel"),
});
