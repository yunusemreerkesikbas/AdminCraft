"use client";

import { useState, type FormEvent } from "react";
import { PageShell } from "@/components/ui/PageShell";
import type { CommerceCustomer } from "@/lib/commerce/customer/types";
import { AddressBookPanel, type AddressBookCopy } from "./AddressBookPanel";
import { useCustomerSession } from "./CustomerSessionProvider";

type AuthMode = "login" | "register";

export type AccountCopy = {
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
  addressBook: AddressBookCopy;
};

type AccountViewProps = {
  copy: AccountCopy;
  ordersHref: string;
};

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

export function AccountView({ copy, ordersHref }: AccountViewProps) {
  const {
    customer,
    isAuthenticated,
    isRestoring,
    isMutating,
    error,
    login,
    register,
    logout,
  } = useCustomerSession();
  const [mode, setMode] = useState<AuthMode>("login");
  const [loginForm, setLoginForm] = useState(defaultLoginState);
  const [registerForm, setRegisterForm] = useState(defaultRegisterState);

  const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await login(loginForm);
  };

  const handleRegister = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await register({
      ...registerForm,
      source: "commerce-ui-account",
    });
  };

  const actions = isAuthenticated ? (
    <button
      className="commerce-action commerce-action--secondary"
      type="button"
      onClick={() => void logout()}
      disabled={isMutating}
    >
      {isMutating ? copy.loggingOutLabel : copy.logoutAction}
    </button>
  ) : null;

  return (
    <PageShell
      eyebrow={copy.eyebrow}
      title={copy.title}
      description={
        isAuthenticated ? copy.authenticatedDescription : copy.description
      }
      actions={actions}
      visual={
        <section className="surface-panel account-status-panel">
          <span className="quiet-chip">
            {isAuthenticated ? copy.profileTitle : copy.loginTab}
          </span>
          <h2 className="frame-title">
            {customer
              ? `${customer.firstName} ${customer.lastName}`
              : copy.profileTitle}
          </h2>
          <p className="frame-note">
            {customer?.email ?? copy.profileDescription}
          </p>
        </section>
      }
    >
      {isRestoring ? (
        <section className="surface-panel account-loading">
          <p className="row-title">{copy.restoringLabel}</p>
          <div className="loading-bar" aria-hidden="true">
            <div className="loading-bar__fill" />
          </div>
        </section>
      ) : isAuthenticated && customer ? (
        <AuthenticatedAccount
          copy={copy}
          customer={customer}
          isMutating={isMutating}
          ordersHref={ordersHref}
          onLogout={logout}
        />
      ) : (
        <section className="account-auth-grid">
          <div className="surface-panel account-auth-panel">
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
                {error || copy.errorFallback}
              </p>
            ) : null}

            {mode === "login" ? (
              <form className="account-form" onSubmit={handleLogin}>
                <AccountField
                  id="customer-login-email"
                  label={copy.emailLabel}
                  type="email"
                  value={loginForm.email}
                  onChange={(email) =>
                    setLoginForm((current) => ({ ...current, email }))
                  }
                  autoComplete="email"
                />
                <AccountField
                  id="customer-login-password"
                  label={copy.passwordLabel}
                  type="password"
                  value={loginForm.password}
                  onChange={(password) =>
                    setLoginForm((current) => ({ ...current, password }))
                  }
                  autoComplete="current-password"
                  minLength={8}
                />
                <AccountCheckbox
                  label={copy.rememberMeLabel}
                  checked={loginForm.rememberMe}
                  onChange={(rememberMe) =>
                    setLoginForm((current) => ({ ...current, rememberMe }))
                  }
                />
                <button
                  className="commerce-action"
                  type="submit"
                  disabled={isMutating}
                >
                  {isMutating ? copy.submittingLabel : copy.loginAction}
                </button>
              </form>
            ) : (
              <form className="account-form" onSubmit={handleRegister}>
                <div className="account-form__split">
                  <AccountField
                    id="customer-register-first-name"
                    label={copy.firstNameLabel}
                    value={registerForm.firstName}
                    onChange={(firstName) =>
                      setRegisterForm((current) => ({ ...current, firstName }))
                    }
                    autoComplete="given-name"
                  />
                  <AccountField
                    id="customer-register-last-name"
                    label={copy.lastNameLabel}
                    value={registerForm.lastName}
                    onChange={(lastName) =>
                      setRegisterForm((current) => ({ ...current, lastName }))
                    }
                    autoComplete="family-name"
                  />
                </div>
                <AccountField
                  id="customer-register-email"
                  label={copy.emailLabel}
                  type="email"
                  value={registerForm.email}
                  onChange={(email) =>
                    setRegisterForm((current) => ({ ...current, email }))
                  }
                  autoComplete="email"
                />
                <AccountField
                  id="customer-register-phone"
                  label={copy.phoneLabel}
                  type="tel"
                  value={registerForm.phone}
                  onChange={(phone) =>
                    setRegisterForm((current) => ({ ...current, phone }))
                  }
                  autoComplete="tel"
                />
                <AccountField
                  id="customer-register-password"
                  label={copy.passwordLabel}
                  type="password"
                  value={registerForm.password}
                  onChange={(password) =>
                    setRegisterForm((current) => ({ ...current, password }))
                  }
                  autoComplete="new-password"
                  minLength={8}
                />
                <AccountCheckbox
                  label={copy.termsAcceptedLabel}
                  checked={registerForm.termsAccepted}
                  required
                  onChange={(termsAccepted) =>
                    setRegisterForm((current) => ({
                      ...current,
                      termsAccepted,
                    }))
                  }
                />
                <AccountCheckbox
                  label={copy.privacyAcceptedLabel}
                  checked={registerForm.privacyAccepted}
                  required
                  onChange={(privacyAccepted) =>
                    setRegisterForm((current) => ({
                      ...current,
                      privacyAccepted,
                    }))
                  }
                />
                <AccountCheckbox
                  label={copy.rememberMeLabel}
                  checked={registerForm.rememberMe}
                  onChange={(rememberMe) =>
                    setRegisterForm((current) => ({ ...current, rememberMe }))
                  }
                />
                <button
                  className="commerce-action"
                  type="submit"
                  disabled={isMutating}
                >
                  {isMutating ? copy.submittingLabel : copy.registerAction}
                </button>
              </form>
            )}
          </div>
        </section>
      )}
    </PageShell>
  );
}

function AccountField({
  id,
  label,
  type = "text",
  value,
  onChange,
  autoComplete,
  minLength,
}: {
  id: string;
  label: string;
  type?: string;
  value: string;
  onChange: (value: string) => void;
  autoComplete?: string;
  minLength?: number;
}) {
  return (
    <label className="account-field" htmlFor={id}>
      <span>{label}</span>
      <input
        id={id}
        type={type}
        value={value}
        minLength={minLength}
        autoComplete={autoComplete}
        required
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function AccountCheckbox({
  label,
  checked,
  required,
  onChange,
}: {
  label: string;
  checked: boolean;
  required?: boolean;
  onChange: (checked: boolean) => void;
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

function AuthenticatedAccount({
  copy,
  customer,
  isMutating,
  ordersHref,
  onLogout,
}: {
  copy: AccountCopy;
  customer: CommerceCustomer;
  isMutating: boolean;
  ordersHref: string;
  onLogout: () => Promise<void>;
}) {
  const displayName = `${customer.firstName} ${customer.lastName}`;

  return (
    <section className="account-dashboard">
      <div className="surface-panel account-profile-panel">
        <div>
          <span className="quiet-chip">{copy.profileTitle}</span>
          <h2 className="frame-title">{displayName}</h2>
          <p className="frame-note">{copy.profileDescription}</p>
        </div>
        <dl className="account-summary">
          <div>
            <dt>{copy.nameLabel}</dt>
            <dd>{displayName}</dd>
          </div>
          <div>
            <dt>{copy.emailLabel}</dt>
            <dd>{customer.email}</dd>
          </div>
          <div>
            <dt>{copy.phoneLabel}</dt>
            <dd>{customer.phone || copy.phoneValueFallback}</dd>
          </div>
          <div>
            <dt>{copy.statusLabel}</dt>
            <dd>{customer.status}</dd>
          </div>
          <div>
            <dt>{copy.emailStatusLabel}</dt>
            <dd>
              <span className="quiet-chip">
                {customer.emailVerified
                  ? copy.verifiedLabel
                  : copy.unverifiedLabel}
              </span>
            </dd>
          </div>
        </dl>
        <button
          className="commerce-action commerce-action--secondary"
          type="button"
          onClick={() => void onLogout()}
          disabled={isMutating}
        >
          {isMutating ? copy.loggingOutLabel : copy.logoutAction}
        </button>
      </div>

      <div className="account-teaser-grid">
        <AccountTeaser
          title={copy.ordersTitle}
          description={copy.ordersDescription}
          label={copy.teaserLabel}
          href={ordersHref}
          action={copy.primaryAction}
        />
      </div>

      <AddressBookPanel copy={copy.addressBook} />
    </section>
  );
}

function AccountTeaser({
  title,
  description,
  label,
  href,
  action,
}: {
  title: string;
  description: string;
  label: string;
  href: string;
  action: string;
}) {
  return (
    <article className="surface-panel account-teaser">
      <span className="quiet-chip">{label}</span>
      <h3 className="row-title">{title}</h3>
      <p className="row-description">{description}</p>
      <a className="commerce-action commerce-action--secondary" href={href}>
        {action}
      </a>
    </article>
  );
}
