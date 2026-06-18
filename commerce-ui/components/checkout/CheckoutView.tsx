"use client";

import { useCallback, useMemo, useState, type FormEvent } from "react";
import { AddressBookPanel, type AddressBookCopy } from "@/components/customer/AddressBookPanel";
import { PageShell } from "@/components/ui/PageShell";
import { ReceiptFrame } from "@/components/ui/StorefrontPrimitives";
import { createCommerceCheckoutClient } from "@/lib/commerce/checkout/checkout-client";
import type { CheckoutResponse } from "@/lib/commerce/checkout/types";
import type { CommerceCustomerAddress } from "@/lib/commerce/customer/types";
import { createCommercePaymentClient } from "@/lib/commerce/payment/payment-client";
import { useCustomerSession } from "@/components/customer/CustomerSessionProvider";

export type CheckoutAuthCopy = {
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
};

export type CheckoutCopy = {
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
  auth: CheckoutAuthCopy;
  addressBook: AddressBookCopy;
};

type CheckoutViewProps = {
  copy: CheckoutCopy;
  apiBaseUrl: string;
  lang: string;
  tenantHeaders: Record<string, string>;
  cartHref: string;
};

type AuthMode = "login" | "register";

const LAST_PAYMENT_ATTEMPT_KEY = "commerce-ui:last-payment-attempt";

const defaultLoginState = {
  email: "",
  password: "",
  rememberMe: false,
};

const defaultRegisterState = {
  email: "",
  password: "",
  firstName: "",
  lastName: "",
  phone: "",
  termsAccepted: false,
  privacyAccepted: false,
  rememberMe: false,
};

const toNumber = (value: number | string | null | undefined): number => {
  if (typeof value === "number") {
    return value;
  }

  if (typeof value === "string") {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  return 0;
};

const formatMoney = (
  lang: string,
  currencyIso: string | undefined,
  value: number | string | null | undefined,
): string =>
  new Intl.NumberFormat(lang, {
    style: "currency",
    currency: currencyIso || "TRY",
  }).format(toNumber(value));

const addressLabel = (address: CommerceCustomerAddress): string =>
  address.label ||
  `${address.firstName} ${address.lastName} - ${address.district}, ${address.city}`;

const shippingMethodLabel = (
  methodNameKey: string | null | undefined,
  copy: CheckoutCopy,
): string => {
  if (methodNameKey === "commerce.shipping.method.standard") {
    return copy.shippingMethodStandardLabel;
  }

  return methodNameKey || copy.shippingMethodFallback;
};

export function CheckoutView({
  copy,
  apiBaseUrl,
  lang,
  tenantHeaders,
  cartHref,
}: CheckoutViewProps) {
  const {
    accessToken,
    isAuthenticated,
    isRestoring,
    isMutating,
    error: sessionError,
    login,
    register,
  } = useCustomerSession();
  const [addresses, setAddresses] = useState<CommerceCustomerAddress[]>([]);
  const [deliveryAddressUid, setDeliveryAddressUid] = useState("");
  const [billingAddressUid, setBillingAddressUid] = useState("");
  const [billingSameAsDelivery, setBillingSameAsDelivery] = useState(true);
  const [checkout, setCheckout] = useState<CheckoutResponse | null>(null);
  const [isCheckoutMutating, setIsCheckoutMutating] = useState(false);
  const [isPaymentMutating, setIsPaymentMutating] = useState(false);
  const [checkoutError, setCheckoutError] = useState<string | null>(null);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  const checkoutClient = useMemo(
    () => createCommerceCheckoutClient({ apiBaseUrl, lang, tenantHeaders }),
    [apiBaseUrl, lang, tenantHeaders],
  );
  const paymentClient = useMemo(
    () => createCommercePaymentClient({ apiBaseUrl, lang, tenantHeaders }),
    [apiBaseUrl, lang, tenantHeaders],
  );

  const markCheckoutDirty = useCallback(() => {
    setCheckout(null);
    setCheckoutError(null);
    setPaymentError(null);
  }, []);

  const syncAddresses = useCallback((nextAddresses: CommerceCustomerAddress[]) => {
    setAddresses(nextAddresses);
    markCheckoutDirty();
    setDeliveryAddressUid((current) => {
      if (current && nextAddresses.some((address) => address.uid === current)) {
        return current;
      }
      return nextAddresses.find((address) => address.defaultDelivery)?.uid ?? "";
    });
    setBillingAddressUid((current) => {
      if (current && nextAddresses.some((address) => address.uid === current)) {
        return current;
      }
      return nextAddresses.find((address) => address.defaultBilling)?.uid ?? "";
    });
  }, [markCheckoutDirty]);

  const submitCheckout = async () => {
    if (!accessToken) {
      return;
    }

    setIsCheckoutMutating(true);
    setCheckoutError(null);
    setPaymentError(null);
    try {
      const request = {
        deliveryAddressUid: deliveryAddressUid || null,
        billingAddressUid: billingSameAsDelivery
          ? null
          : billingAddressUid || null,
        billingSameAsDelivery,
      };
      const nextCheckout = checkout
        ? await checkoutClient.updateCheckoutAddresses(
            accessToken,
            checkout.checkoutUid,
            request,
          )
        : await checkoutClient.startCheckout(accessToken, request);
      setCheckout(nextCheckout);
    } catch (err) {
      setCheckoutError(err instanceof Error ? err.message : "");
    } finally {
      setIsCheckoutMutating(false);
    }
  };

  const startPayment = async () => {
    if (!accessToken || !checkout?.validation.valid) {
      return;
    }

    setIsPaymentMutating(true);
    setPaymentError(null);
    try {
      const attempt = await paymentClient.createPaymentAttempt(
        accessToken,
        checkout.checkoutUid,
      );
      window.sessionStorage.setItem(
        LAST_PAYMENT_ATTEMPT_KEY,
        JSON.stringify({
          attemptUid: attempt.attemptUid,
          checkoutUid: attempt.checkoutUid,
          status: attempt.status,
        }),
      );
      const initialize = await paymentClient.initializePaymentAttempt(
        accessToken,
        attempt.attemptUid,
      );
      window.sessionStorage.setItem(
        LAST_PAYMENT_ATTEMPT_KEY,
        JSON.stringify({
          attemptUid: initialize.attemptUid,
          checkoutUid: attempt.checkoutUid,
          status: initialize.status,
        }),
      );
      window.location.assign(initialize.paymentPageUrl);
    } catch (err) {
      setPaymentError(err instanceof Error ? err.message : "");
      setIsPaymentMutating(false);
    }
  };

  const currencyIso = checkout?.totals.currencyIso;
  const summaryRows = [
    {
      label: copy.rowStatus,
      value: checkout?.status ?? copy.summaryNote,
    },
    {
      label: copy.rowShipping,
      value: shippingMethodLabel(checkout?.shipping.methodNameKey, copy),
    },
    {
      label: copy.rowSubtotal,
      value: formatMoney(lang, currencyIso, checkout?.totals.subtotal),
    },
    {
      label: copy.rowVat,
      value: formatMoney(lang, currencyIso, checkout?.totals.vatTotal),
    },
    {
      label: copy.rowShippingTotal,
      value: formatMoney(lang, currencyIso, checkout?.totals.shippingTotal),
    },
  ];
  const expectedBillingAddressUid = billingSameAsDelivery
    ? deliveryAddressUid
    : billingAddressUid;
  const checkoutBillingAddressUid =
    checkout?.billingAddress?.uid ??
    (billingSameAsDelivery ? checkout?.deliveryAddress?.uid : "");
  const checkoutMatchesSelection =
    checkout?.deliveryAddress?.uid === deliveryAddressUid &&
    checkoutBillingAddressUid === expectedBillingAddressUid;
  const canStartPayment =
    Boolean(accessToken && checkout?.validation.valid && checkoutMatchesSelection) &&
    !isMutating &&
    !isCheckoutMutating &&
    !isPaymentMutating;

  return (
    <PageShell
      eyebrow={copy.eyebrow}
      title={copy.title}
      description={
        isAuthenticated ? copy.authenticatedDescription : copy.description
      }
      actions={
        <>
          <button
            type="button"
            className="commerce-action"
            onClick={() => void startPayment()}
            disabled={!canStartPayment}
          >
            {isPaymentMutating
              ? copy.paymentPreparingLabel
              : canStartPayment
                ? copy.paymentAction
                : copy.paymentDisabled}
          </button>
          <a
            href={cartHref}
            className="commerce-action commerce-action--secondary"
          >
            {copy.secondaryAction}
          </a>
        </>
      }
      visual={
        <ReceiptFrame
          title={copy.summaryTitle}
          note={checkout ? undefined : copy.summaryNote}
          rows={summaryRows}
          totalLabel={copy.totalLabel}
          totalValue={formatMoney(lang, currencyIso, checkout?.totals.total)}
        />
      }
    >
      {isRestoring ? (
        <section className="surface-panel checkout-panel">
          <p className="frame-note">{copy.loadingLabel}</p>
        </section>
      ) : !isAuthenticated ? (
        <CheckoutAuthGate
          copy={copy.auth}
          error={sessionError}
          errorFallback={copy.errorFallback}
          isMutating={isMutating}
          onLogin={login}
          onRegister={register}
        />
      ) : (
        <section className="checkout-grid">
          <div className="checkout-main">
            <section className="surface-panel checkout-panel">
              <h2 className="frame-title">{copy.addressStepTitle}</h2>
              <p className="frame-note">{copy.addressStepDescription}</p>
              <div className="checkout-address-controls">
                <label className="account-field" htmlFor="delivery-address">
                  <span>{copy.deliveryAddressLabel}</span>
                  <select
                    id="delivery-address"
                    value={deliveryAddressUid}
                    onChange={(event) => {
                      setDeliveryAddressUid(event.target.value);
                      markCheckoutDirty();
                    }}
                  >
                    <option value="">{copy.noAddressOption}</option>
                    {addresses.map((address) => (
                      <option key={address.uid} value={address.uid}>
                        {addressLabel(address)}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="account-checkbox">
                  <input
                    type="checkbox"
                    checked={billingSameAsDelivery}
                    onChange={(event) => {
                      setBillingSameAsDelivery(event.target.checked);
                      markCheckoutDirty();
                    }}
                  />
                  <span>{copy.billingSameAsDeliveryLabel}</span>
                </label>
                {!billingSameAsDelivery ? (
                  <label className="account-field" htmlFor="billing-address">
                    <span>{copy.billingAddressLabel}</span>
                    <select
                      id="billing-address"
                      value={billingAddressUid}
                      onChange={(event) => {
                        setBillingAddressUid(event.target.value);
                        markCheckoutDirty();
                      }}
                    >
                      <option value="">{copy.noAddressOption}</option>
                      {addresses.map((address) => (
                        <option key={address.uid} value={address.uid}>
                          {addressLabel(address)}
                        </option>
                      ))}
                    </select>
                  </label>
                ) : null}
              </div>
              {checkoutError || paymentError || sessionError ? (
                <p className="account-form-error" role="alert">
                  {checkoutError ||
                    paymentError ||
                    sessionError ||
                    copy.errorFallback}
                </p>
              ) : null}
              <button
                type="button"
                className="commerce-action"
                onClick={() => void submitCheckout()}
                disabled={
                  isMutating ||
                  isCheckoutMutating ||
                  addresses.length === 0 ||
                  !deliveryAddressUid
                }
              >
                {isCheckoutMutating
                  ? copy.loadingLabel
                  : checkout
                    ? copy.updateAction
                    : copy.startAction}
              </button>
            </section>

            <AddressBookPanel
              copy={copy.addressBook}
              onAddressesChange={syncAddresses}
            />
          </div>

          <CheckoutSnapshot copy={copy} checkout={checkout} lang={lang} />
        </section>
      )}
    </PageShell>
  );
}

function CheckoutAuthGate({
  copy,
  error,
  errorFallback,
  isMutating,
  onLogin,
  onRegister,
}: {
  copy: CheckoutAuthCopy;
  error: string | null;
  errorFallback: string;
  isMutating: boolean;
  onLogin: (request: typeof defaultLoginState) => Promise<boolean>;
  onRegister: (
    request: typeof defaultRegisterState & { source: string },
  ) => Promise<boolean>;
}) {
  const [mode, setMode] = useState<AuthMode>("login");
  const [loginForm, setLoginForm] = useState(defaultLoginState);
  const [registerForm, setRegisterForm] = useState(defaultRegisterState);

  const submitLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await onLogin(loginForm);
  };

  const submitRegister = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await onRegister({
      ...registerForm,
      source: "commerce-ui-checkout",
    });
  };

  return (
    <section className="surface-panel account-auth-panel">
      <h2 className="frame-title">{copy.title}</h2>
      <p className="frame-note">{copy.description}</p>
      <div className="account-tabs" role="tablist" aria-label={copy.title}>
        <button
          type="button"
          role="tab"
          aria-selected={mode === "login"}
          className="account-tab"
          onClick={() => setMode("login")}
        >
          {copy.loginTab}
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={mode === "register"}
          className="account-tab"
          onClick={() => setMode("register")}
        >
          {copy.registerTab}
        </button>
      </div>

      {error !== null ? (
        <p className="account-form-error" role="alert">
          {error || errorFallback}
        </p>
      ) : null}

      {mode === "login" ? (
        <form className="account-form" onSubmit={submitLogin}>
          <CheckoutAuthField
            id="checkout-login-email"
            label={copy.emailLabel}
            type="email"
            value={loginForm.email}
            onChange={(email) =>
              setLoginForm((current) => ({ ...current, email }))
            }
          />
          <CheckoutAuthField
            id="checkout-login-password"
            label={copy.passwordLabel}
            type="password"
            value={loginForm.password}
            onChange={(password) =>
              setLoginForm((current) => ({ ...current, password }))
            }
          />
          <CheckoutAuthCheckbox
            label={copy.rememberMeLabel}
            checked={loginForm.rememberMe}
            onChange={(rememberMe) =>
              setLoginForm((current) => ({ ...current, rememberMe }))
            }
          />
          <button type="submit" className="commerce-action" disabled={isMutating}>
            {isMutating ? copy.submittingLabel : copy.loginAction}
          </button>
        </form>
      ) : (
        <form className="account-form" onSubmit={submitRegister}>
          <div className="account-form__split">
            <CheckoutAuthField
              id="checkout-register-first-name"
              label={copy.firstNameLabel}
              value={registerForm.firstName}
              onChange={(firstName) =>
                setRegisterForm((current) => ({ ...current, firstName }))
              }
            />
            <CheckoutAuthField
              id="checkout-register-last-name"
              label={copy.lastNameLabel}
              value={registerForm.lastName}
              onChange={(lastName) =>
                setRegisterForm((current) => ({ ...current, lastName }))
              }
            />
          </div>
          <CheckoutAuthField
            id="checkout-register-email"
            label={copy.emailLabel}
            type="email"
            value={registerForm.email}
            onChange={(email) =>
              setRegisterForm((current) => ({ ...current, email }))
            }
          />
          <CheckoutAuthField
            id="checkout-register-phone"
            label={copy.phoneLabel}
            type="tel"
            value={registerForm.phone}
            onChange={(phone) =>
              setRegisterForm((current) => ({ ...current, phone }))
            }
          />
          <CheckoutAuthField
            id="checkout-register-password"
            label={copy.passwordLabel}
            type="password"
            value={registerForm.password}
            onChange={(password) =>
              setRegisterForm((current) => ({ ...current, password }))
            }
          />
          <CheckoutAuthCheckbox
            label={copy.termsAcceptedLabel}
            checked={registerForm.termsAccepted}
            required
            onChange={(termsAccepted) =>
              setRegisterForm((current) => ({ ...current, termsAccepted }))
            }
          />
          <CheckoutAuthCheckbox
            label={copy.privacyAcceptedLabel}
            checked={registerForm.privacyAccepted}
            required
            onChange={(privacyAccepted) =>
              setRegisterForm((current) => ({ ...current, privacyAccepted }))
            }
          />
          <CheckoutAuthCheckbox
            label={copy.rememberMeLabel}
            checked={registerForm.rememberMe}
            onChange={(rememberMe) =>
              setRegisterForm((current) => ({ ...current, rememberMe }))
            }
          />
          <button type="submit" className="commerce-action" disabled={isMutating}>
            {isMutating ? copy.submittingLabel : copy.registerAction}
          </button>
        </form>
      )}
    </section>
  );
}

function CheckoutSnapshot({
  copy,
  checkout,
  lang,
}: {
  copy: CheckoutCopy;
  checkout: CheckoutResponse | null;
  lang: string;
}) {
  if (!checkout) {
    return (
      <aside className="surface-panel checkout-panel">
        <h2 className="frame-title">{copy.itemSummaryTitle}</h2>
        <p className="frame-note">{copy.summaryNote}</p>
      </aside>
    );
  }

  return (
    <aside className="surface-panel checkout-panel checkout-snapshot">
      <h2 className="frame-title">{copy.validationTitle}</h2>
      <div className="checkout-validation">
        <span className="quiet-chip">
          {checkout.validation.valid ? copy.validLabel : copy.invalidLabel}
        </span>
        {checkout.validation.cartChanged ? (
          <span className="quiet-chip">{copy.cartChangedLabel}</span>
        ) : null}
        {checkout.validation.priceChanged ? (
          <span className="quiet-chip">{copy.priceChangedLabel}</span>
        ) : null}
        {checkout.validation.stockChanged ? (
          <span className="quiet-chip">{copy.stockChangedLabel}</span>
        ) : null}
      </div>
      {checkout.validation.warningMessageKeys.length > 0 ? (
        <p className="frame-note">
          {copy.warningKeysLabel}:{" "}
          {checkout.validation.warningMessageKeys.join(", ")}
        </p>
      ) : null}
      <h3 className="row-title">{copy.itemSummaryTitle}</h3>
      <div className="checkout-items">
        {checkout.items.map((item) => (
          <article key={item.uid} className="checkout-item-row">
            <span>
              {item.productSku || item.variantSku || item.productUid || copy.itemFallback}
            </span>
            <strong>
              {item.quantity} x{" "}
              {formatMoney(lang, checkout.totals.currencyIso, item.unitGrossPrice)}
            </strong>
          </article>
        ))}
      </div>
      <p className="frame-note">
        {checkout.validation.valid ? copy.paymentAction : copy.paymentDisabled}
      </p>
    </aside>
  );
}

function CheckoutAuthField({
  id,
  label,
  value,
  onChange,
  type = "text",
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  type?: string;
}) {
  return (
    <label className="account-field" htmlFor={id}>
      <span>{label}</span>
      <input
        id={id}
        type={type}
        value={value}
        required
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function CheckoutAuthCheckbox({
  label,
  checked,
  onChange,
  required,
}: {
  label: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  required?: boolean;
}) {
  return (
    <label className="account-checkbox">
      <input
        type="checkbox"
        checked={checked}
        required={required}
        onChange={(event) => onChange(event.target.checked)}
      />
      <span>{label}</span>
    </label>
  );
}
