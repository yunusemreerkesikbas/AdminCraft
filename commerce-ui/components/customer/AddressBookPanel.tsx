"use client";

import { useCallback, useEffect, useState, type FormEvent } from "react";
import type {
  CommerceCustomerAddress,
  CommerceCustomerAddressRequest,
  InvoiceType,
} from "@/lib/commerce/customer/types";
import { useCustomerSession } from "./CustomerSessionProvider";

export type AddressBookCopy = {
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

type AddressBookPanelProps = {
  copy: AddressBookCopy;
  onAddressesChange?: (addresses: CommerceCustomerAddress[]) => void;
};

type AddressFormState = {
  label: string;
  firstName: string;
  lastName: string;
  phone: string;
  countryIso: string;
  city: string;
  district: string;
  addressLine1: string;
  addressLine2: string;
  postalCode: string;
  defaultDelivery: boolean;
  defaultBilling: boolean;
  invoiceType: InvoiceType;
  companyName: string;
  taxNumber: string;
  taxOffice: string;
  invoiceIdentityNumber: string;
};

const defaultFormState: AddressFormState = {
  label: "",
  firstName: "",
  lastName: "",
  phone: "",
  countryIso: "TR",
  city: "",
  district: "",
  addressLine1: "",
  addressLine2: "",
  postalCode: "",
  defaultDelivery: false,
  defaultBilling: false,
  invoiceType: "INDIVIDUAL",
  companyName: "",
  taxNumber: "",
  taxOffice: "",
  invoiceIdentityNumber: "",
};

const valueOrEmpty = (value: string | null | undefined): string => value ?? "";

const toFormState = (address: CommerceCustomerAddress): AddressFormState => ({
  label: valueOrEmpty(address.label),
  firstName: address.firstName,
  lastName: address.lastName,
  phone: address.phone,
  countryIso: address.countryIso || "TR",
  city: address.city,
  district: address.district,
  addressLine1: address.addressLine1,
  addressLine2: valueOrEmpty(address.addressLine2),
  postalCode: valueOrEmpty(address.postalCode),
  defaultDelivery: address.defaultDelivery,
  defaultBilling: address.defaultBilling,
  invoiceType: address.invoiceType,
  companyName: valueOrEmpty(address.companyName),
  taxNumber: valueOrEmpty(address.taxNumber),
  taxOffice: valueOrEmpty(address.taxOffice),
  invoiceIdentityNumber: valueOrEmpty(address.invoiceIdentityNumber),
});

const emptyToNull = (value: string): string | null => {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
};

const toRequest = (form: AddressFormState): CommerceCustomerAddressRequest => ({
  label: emptyToNull(form.label),
  firstName: form.firstName,
  lastName: form.lastName,
  phone: form.phone,
  countryIso: form.countryIso || "TR",
  city: form.city,
  district: form.district,
  addressLine1: form.addressLine1,
  addressLine2: emptyToNull(form.addressLine2),
  postalCode: emptyToNull(form.postalCode),
  defaultDelivery: form.defaultDelivery,
  defaultBilling: form.defaultBilling,
  invoiceType: form.invoiceType,
  companyName:
    form.invoiceType === "CORPORATE" ? emptyToNull(form.companyName) : null,
  taxNumber:
    form.invoiceType === "CORPORATE" ? emptyToNull(form.taxNumber) : null,
  taxOffice:
    form.invoiceType === "CORPORATE" ? emptyToNull(form.taxOffice) : null,
  invoiceIdentityNumber:
    form.invoiceType === "INDIVIDUAL"
      ? emptyToNull(form.invoiceIdentityNumber)
      : null,
});

export function AddressBookPanel({
  copy,
  onAddressesChange,
}: AddressBookPanelProps) {
  const {
    accessToken,
    isMutating,
    error,
    listAddresses,
    createAddress,
    updateAddress,
    deleteAddress,
    setDefaultDelivery,
    setDefaultBilling,
  } = useCustomerSession();
  const [addresses, setAddresses] = useState<CommerceCustomerAddress[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingUid, setEditingUid] = useState<string | null>(null);
  const [form, setForm] = useState<AddressFormState>(defaultFormState);
  const [localError, setLocalError] = useState<string | null>(null);

  const applyAddresses = useCallback(
    (nextAddresses: CommerceCustomerAddress[]) => {
      setAddresses(nextAddresses);
      onAddressesChange?.(nextAddresses);
    },
    [onAddressesChange],
  );

  const loadAddresses = useCallback(async () => {
    if (!accessToken) {
      applyAddresses([]);
      return;
    }

    setIsLoading(true);
    setLocalError(null);
    try {
      applyAddresses(await listAddresses());
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "");
    } finally {
      setIsLoading(false);
    }
  }, [accessToken, applyAddresses, listAddresses]);

  useEffect(() => {
    void loadAddresses();
  }, [loadAddresses]);

  const openCreateForm = () => {
    setEditingUid(null);
    setForm(defaultFormState);
    setIsFormOpen(true);
  };

  const openEditForm = (address: CommerceCustomerAddress) => {
    setEditingUid(address.uid);
    setForm(toFormState(address));
    setIsFormOpen(true);
  };

  const closeForm = () => {
    setEditingUid(null);
    setForm(defaultFormState);
    setIsFormOpen(false);
  };

  const submitForm = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const request = toRequest(form);
    const saved = editingUid
      ? await updateAddress(editingUid, request)
      : await createAddress(request);

    if (saved) {
      closeForm();
      await loadAddresses();
    }
  };

  const removeAddress = async (addressUid: string) => {
    const deleted = await deleteAddress(addressUid);
    if (deleted) {
      await loadAddresses();
    }
  };

  const markDefaultDelivery = async (addressUid: string) => {
    const updated = await setDefaultDelivery(addressUid);
    if (updated) {
      await loadAddresses();
    }
  };

  const markDefaultBilling = async (addressUid: string) => {
    const updated = await setDefaultBilling(addressUid);
    if (updated) {
      await loadAddresses();
    }
  };

  const visibleError = localError || error;

  if (!accessToken) {
    return (
      <section className="surface-panel address-panel">
        <h2 className="frame-title">{copy.title}</h2>
        <p className="frame-note">{copy.authRequiredLabel}</p>
      </section>
    );
  }

  return (
    <section className="surface-panel address-panel">
      <div className="address-panel__header">
        <div>
          <h2 className="frame-title">{copy.title}</h2>
          <p className="frame-note">{copy.description}</p>
        </div>
        <button
          type="button"
          className="commerce-action commerce-action--secondary"
          onClick={openCreateForm}
          disabled={isMutating}
        >
          {copy.addAction}
        </button>
      </div>

      {visibleError ? (
        <p className="account-form-error" role="alert">
          {visibleError || copy.errorFallback}
        </p>
      ) : null}

      {isLoading ? (
        <p className="frame-note">{copy.loadingLabel}</p>
      ) : addresses.length === 0 ? (
        <div className="address-empty">
          <h3 className="row-title">{copy.emptyTitle}</h3>
          <p className="row-description">{copy.emptyDescription}</p>
        </div>
      ) : (
        <div className="address-list">
          {addresses.map((address) => (
            <article key={address.uid} className="address-card">
              <div className="address-card__body">
                <h3 className="row-title">
                  {address.label || `${address.firstName} ${address.lastName}`}
                </h3>
                <p className="row-description">
                  {address.addressLine1}, {address.district}, {address.city}
                </p>
                <p className="row-description">
                  {address.firstName} {address.lastName} - {address.phone}
                </p>
                <div className="address-card__chips">
                  {address.defaultDelivery ? (
                    <span className="quiet-chip">
                      {copy.defaultDeliveryLabel}
                    </span>
                  ) : null}
                  {address.defaultBilling ? (
                    <span className="quiet-chip">{copy.defaultBillingLabel}</span>
                  ) : null}
                  <span className="quiet-chip">
                    {address.invoiceType === "CORPORATE"
                      ? copy.corporateLabel
                      : copy.individualLabel}
                  </span>
                </div>
              </div>
              <div className="address-card__actions">
                <button
                  type="button"
                  className="cart-text-button"
                  onClick={() => openEditForm(address)}
                  disabled={isMutating}
                >
                  {copy.editAction}
                </button>
                <button
                  type="button"
                  className="cart-text-button"
                  onClick={() => markDefaultDelivery(address.uid)}
                  disabled={isMutating || address.defaultDelivery}
                >
                  {copy.defaultDeliveryAction}
                </button>
                <button
                  type="button"
                  className="cart-text-button"
                  onClick={() => markDefaultBilling(address.uid)}
                  disabled={isMutating || address.defaultBilling}
                >
                  {copy.defaultBillingAction}
                </button>
                <button
                  type="button"
                  className="cart-text-button"
                  onClick={() => removeAddress(address.uid)}
                  disabled={isMutating}
                >
                  {copy.deleteAction}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}

      {isFormOpen ? (
        <form className="address-form" onSubmit={submitForm}>
          <h3 className="row-title">
            {editingUid ? copy.formTitleEdit : copy.formTitleCreate}
          </h3>
          <AddressField
            id="address-label"
            label={copy.labelLabel}
            value={form.label}
            onChange={(label) => setForm((current) => ({ ...current, label }))}
          />
          <div className="address-form__split">
            <AddressField
              id="address-first-name"
              label={copy.firstNameLabel}
              value={form.firstName}
              required
              onChange={(firstName) =>
                setForm((current) => ({ ...current, firstName }))
              }
            />
            <AddressField
              id="address-last-name"
              label={copy.lastNameLabel}
              value={form.lastName}
              required
              onChange={(lastName) =>
                setForm((current) => ({ ...current, lastName }))
              }
            />
          </div>
          <div className="address-form__split">
            <AddressField
              id="address-phone"
              label={copy.phoneLabel}
              type="tel"
              value={form.phone}
              required
              onChange={(phone) => setForm((current) => ({ ...current, phone }))}
            />
            <AddressField
              id="address-country"
              label={copy.countryLabel}
              value={form.countryIso}
              required
              maxLength={2}
              onChange={(countryIso) =>
                setForm((current) => ({
                  ...current,
                  countryIso: countryIso.toUpperCase(),
                }))
              }
            />
          </div>
          <div className="address-form__split">
            <AddressField
              id="address-city"
              label={copy.cityLabel}
              value={form.city}
              required
              onChange={(city) => setForm((current) => ({ ...current, city }))}
            />
            <AddressField
              id="address-district"
              label={copy.districtLabel}
              value={form.district}
              required
              onChange={(district) =>
                setForm((current) => ({ ...current, district }))
              }
            />
          </div>
          <AddressField
            id="address-line-1"
            label={copy.addressLine1Label}
            value={form.addressLine1}
            required
            onChange={(addressLine1) =>
              setForm((current) => ({ ...current, addressLine1 }))
            }
          />
          <div className="address-form__split">
            <AddressField
              id="address-line-2"
              label={copy.addressLine2Label}
              value={form.addressLine2}
              onChange={(addressLine2) =>
                setForm((current) => ({ ...current, addressLine2 }))
              }
            />
            <AddressField
              id="address-postal-code"
              label={copy.postalCodeLabel}
              value={form.postalCode}
              onChange={(postalCode) =>
                setForm((current) => ({ ...current, postalCode }))
              }
            />
          </div>
          <label className="account-field" htmlFor="address-invoice-type">
            <span>{copy.invoiceTypeLabel}</span>
            <select
              id="address-invoice-type"
              value={form.invoiceType}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  invoiceType: event.target.value as InvoiceType,
                }))
              }
            >
              <option value="INDIVIDUAL">{copy.individualLabel}</option>
              <option value="CORPORATE">{copy.corporateLabel}</option>
            </select>
          </label>
          {form.invoiceType === "CORPORATE" ? (
            <>
              <AddressField
                id="address-company-name"
                label={copy.companyNameLabel}
                value={form.companyName}
                required
                onChange={(companyName) =>
                  setForm((current) => ({ ...current, companyName }))
                }
              />
              <div className="address-form__split">
                <AddressField
                  id="address-tax-number"
                  label={copy.taxNumberLabel}
                  value={form.taxNumber}
                  required
                  onChange={(taxNumber) =>
                    setForm((current) => ({ ...current, taxNumber }))
                  }
                />
                <AddressField
                  id="address-tax-office"
                  label={copy.taxOfficeLabel}
                  value={form.taxOffice}
                  required
                  onChange={(taxOffice) =>
                    setForm((current) => ({ ...current, taxOffice }))
                  }
                />
              </div>
            </>
          ) : (
            <AddressField
              id="address-invoice-identity"
              label={copy.invoiceIdentityNumberLabel}
              value={form.invoiceIdentityNumber}
              onChange={(invoiceIdentityNumber) =>
                setForm((current) => ({ ...current, invoiceIdentityNumber }))
              }
            />
          )}
          <div className="address-form__checks">
            <AddressCheckbox
              label={copy.defaultDeliveryInputLabel}
              checked={form.defaultDelivery}
              onChange={(defaultDelivery) =>
                setForm((current) => ({ ...current, defaultDelivery }))
              }
            />
            <AddressCheckbox
              label={copy.defaultBillingInputLabel}
              checked={form.defaultBilling}
              onChange={(defaultBilling) =>
                setForm((current) => ({ ...current, defaultBilling }))
              }
            />
          </div>
          <div className="address-form__actions">
            <button
              type="submit"
              className="commerce-action"
              disabled={isMutating}
            >
              {copy.saveAction}
            </button>
            <button
              type="button"
              className="commerce-action commerce-action--secondary"
              onClick={closeForm}
              disabled={isMutating}
            >
              {copy.cancelAction}
            </button>
          </div>
        </form>
      ) : null}
    </section>
  );
}

function AddressField({
  id,
  label,
  value,
  onChange,
  type = "text",
  required,
  maxLength,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  type?: string;
  required?: boolean;
  maxLength?: number;
}) {
  return (
    <label className="account-field" htmlFor={id}>
      <span>{label}</span>
      <input
        id={id}
        type={type}
        value={value}
        required={required}
        maxLength={maxLength}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function AddressCheckbox({
  label,
  checked,
  onChange,
}: {
  label: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
}) {
  return (
    <label className="account-checkbox">
      <input
        type="checkbox"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
      />
      <span>{label}</span>
    </label>
  );
}
