"use client";

import { useCallback, useEffect, useState, type FormEvent } from "react";
import type {
  CommerceCustomerAddress,
  CommerceCustomerAddressRequest,
  InvoiceType,
} from "@/lib/commerce/customer/types";
import type { AddressBookModel } from "./address-book-model";
import { useCustomerSession } from "./CustomerSessionProvider";

type AddressBookPanelProps = {
  model: AddressBookModel;
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
  model,
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
        <h2 className="frame-title">{model.title}</h2>
        <p className="frame-note">{model.authRequiredLabel}</p>
      </section>
    );
  }

  return (
    <section className="surface-panel address-panel">
      <div className="address-panel__header">
        <div>
          <h2 className="frame-title">{model.title}</h2>
          <p className="frame-note">{model.description}</p>
        </div>
        <button
          type="button"
          className="commerce-action commerce-action--secondary"
          onClick={openCreateForm}
          disabled={isMutating}
        >
          {model.addAction}
        </button>
      </div>

      {visibleError ? (
        <p className="account-form-error" role="alert">
          {visibleError || model.errorFallback}
        </p>
      ) : null}

      {isLoading ? (
        <p className="frame-note">{model.loadingLabel}</p>
      ) : addresses.length === 0 ? (
        <div className="address-empty">
          <h3 className="row-title">{model.emptyTitle}</h3>
          <p className="row-description">{model.emptyDescription}</p>
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
                      {model.defaultDeliveryLabel}
                    </span>
                  ) : null}
                  {address.defaultBilling ? (
                    <span className="quiet-chip">{model.defaultBillingLabel}</span>
                  ) : null}
                  <span className="quiet-chip">
                    {address.invoiceType === "CORPORATE"
                      ? model.corporateLabel
                      : model.individualLabel}
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
                  {model.editAction}
                </button>
                <button
                  type="button"
                  className="cart-text-button"
                  onClick={() => markDefaultDelivery(address.uid)}
                  disabled={isMutating || address.defaultDelivery}
                >
                  {model.defaultDeliveryAction}
                </button>
                <button
                  type="button"
                  className="cart-text-button"
                  onClick={() => markDefaultBilling(address.uid)}
                  disabled={isMutating || address.defaultBilling}
                >
                  {model.defaultBillingAction}
                </button>
                <button
                  type="button"
                  className="cart-text-button"
                  onClick={() => removeAddress(address.uid)}
                  disabled={isMutating}
                >
                  {model.deleteAction}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}

      {isFormOpen ? (
        <form className="address-form" onSubmit={submitForm}>
          <h3 className="row-title">
            {editingUid ? model.formTitleEdit : model.formTitleCreate}
          </h3>
          <AddressField
            id="address-label"
            label={model.labelLabel}
            value={form.label}
            onChange={(label) => setForm((current) => ({ ...current, label }))}
          />
          <div className="address-form__split">
            <AddressField
              id="address-first-name"
              label={model.firstNameLabel}
              value={form.firstName}
              required
              onChange={(firstName) =>
                setForm((current) => ({ ...current, firstName }))
              }
            />
            <AddressField
              id="address-last-name"
              label={model.lastNameLabel}
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
              label={model.phoneLabel}
              type="tel"
              value={form.phone}
              required
              onChange={(phone) => setForm((current) => ({ ...current, phone }))}
            />
            <AddressField
              id="address-country"
              label={model.countryLabel}
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
              label={model.cityLabel}
              value={form.city}
              required
              onChange={(city) => setForm((current) => ({ ...current, city }))}
            />
            <AddressField
              id="address-district"
              label={model.districtLabel}
              value={form.district}
              required
              onChange={(district) =>
                setForm((current) => ({ ...current, district }))
              }
            />
          </div>
          <AddressField
            id="address-line-1"
            label={model.addressLine1Label}
            value={form.addressLine1}
            required
            onChange={(addressLine1) =>
              setForm((current) => ({ ...current, addressLine1 }))
            }
          />
          <div className="address-form__split">
            <AddressField
              id="address-line-2"
              label={model.addressLine2Label}
              value={form.addressLine2}
              onChange={(addressLine2) =>
                setForm((current) => ({ ...current, addressLine2 }))
              }
            />
            <AddressField
              id="address-postal-code"
              label={model.postalCodeLabel}
              value={form.postalCode}
              onChange={(postalCode) =>
                setForm((current) => ({ ...current, postalCode }))
              }
            />
          </div>
          <label className="account-field" htmlFor="address-invoice-type">
            <span>{model.invoiceTypeLabel}</span>
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
              <option value="INDIVIDUAL">{model.individualLabel}</option>
              <option value="CORPORATE">{model.corporateLabel}</option>
            </select>
          </label>
          {form.invoiceType === "CORPORATE" ? (
            <>
              <AddressField
                id="address-company-name"
                label={model.companyNameLabel}
                value={form.companyName}
                required
                onChange={(companyName) =>
                  setForm((current) => ({ ...current, companyName }))
                }
              />
              <div className="address-form__split">
                <AddressField
                  id="address-tax-number"
                  label={model.taxNumberLabel}
                  value={form.taxNumber}
                  required
                  onChange={(taxNumber) =>
                    setForm((current) => ({ ...current, taxNumber }))
                  }
                />
                <AddressField
                  id="address-tax-office"
                  label={model.taxOfficeLabel}
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
              label={model.invoiceIdentityNumberLabel}
              value={form.invoiceIdentityNumber}
              onChange={(invoiceIdentityNumber) =>
                setForm((current) => ({ ...current, invoiceIdentityNumber }))
              }
            />
          )}
          <div className="address-form__checks">
            <AddressCheckbox
              label={model.defaultDeliveryInputLabel}
              checked={form.defaultDelivery}
              onChange={(defaultDelivery) =>
                setForm((current) => ({ ...current, defaultDelivery }))
              }
            />
            <AddressCheckbox
              label={model.defaultBillingInputLabel}
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
              {model.saveAction}
            </button>
            <button
              type="button"
              className="commerce-action commerce-action--secondary"
              onClick={closeForm}
              disabled={isMutating}
            >
              {model.cancelAction}
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
