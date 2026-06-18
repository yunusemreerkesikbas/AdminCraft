"use client";

import { useState, type FormEvent } from "react";
import type { InlineCustomerAuthModel } from "./inline-customer-auth-model";
import { useCustomerSession } from "./CustomerSessionProvider";

type InlineCustomerAuthGateProps = {
  model: InlineCustomerAuthModel;
  source: string;
};

type AuthMode = "login" | "register";

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

export function InlineCustomerAuthGate({
  model,
  source,
}: InlineCustomerAuthGateProps) {
  const { error, isMutating, login, register } = useCustomerSession();
  const [mode, setMode] = useState<AuthMode>("login");
  const [loginForm, setLoginForm] = useState(defaultLoginState);
  const [registerForm, setRegisterForm] = useState(defaultRegisterState);

  const submitLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await login(loginForm);
  };

  const submitRegister = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await register({
      ...registerForm,
      source,
    });
  };

  return (
    <section className="surface-panel account-auth-panel">
      <h2 className="frame-title">{model.title}</h2>
      <p className="frame-note">{model.description}</p>
      <div className="account-tabs" role="group" aria-label={model.title}>
        <button
          type="button"
          aria-pressed={mode === "login"}
          className="account-tab"
          onClick={() => setMode("login")}
        >
          {model.loginTab}
        </button>
        <button
          type="button"
          aria-pressed={mode === "register"}
          className="account-tab"
          onClick={() => setMode("register")}
        >
          {model.registerTab}
        </button>
      </div>

      {error !== null ? (
        <p className="account-form-error" role="alert">
          {error || model.errorFallback}
        </p>
      ) : null}

      {mode === "login" ? (
        <form className="account-form" onSubmit={submitLogin}>
          <AuthField
            id={`${source}-login-email`}
            label={model.emailLabel}
            type="email"
            value={loginForm.email}
            autoComplete="email"
            onChange={(email) =>
              setLoginForm((current) => ({ ...current, email }))
            }
          />
          <AuthField
            id={`${source}-login-password`}
            label={model.passwordLabel}
            type="password"
            value={loginForm.password}
            autoComplete="current-password"
            minLength={8}
            onChange={(password) =>
              setLoginForm((current) => ({ ...current, password }))
            }
          />
          <AuthCheckbox
            label={model.rememberMeLabel}
            checked={loginForm.rememberMe}
            onChange={(rememberMe) =>
              setLoginForm((current) => ({ ...current, rememberMe }))
            }
          />
          <button className="commerce-action" type="submit" disabled={isMutating}>
            {isMutating ? model.submittingLabel : model.loginAction}
          </button>
        </form>
      ) : (
        <form className="account-form" onSubmit={submitRegister}>
          <div className="account-form__split">
            <AuthField
              id={`${source}-register-first-name`}
              label={model.firstNameLabel}
              value={registerForm.firstName}
              autoComplete="given-name"
              onChange={(firstName) =>
                setRegisterForm((current) => ({ ...current, firstName }))
              }
            />
            <AuthField
              id={`${source}-register-last-name`}
              label={model.lastNameLabel}
              value={registerForm.lastName}
              autoComplete="family-name"
              onChange={(lastName) =>
                setRegisterForm((current) => ({ ...current, lastName }))
              }
            />
          </div>
          <AuthField
            id={`${source}-register-email`}
            label={model.emailLabel}
            type="email"
            value={registerForm.email}
            autoComplete="email"
            onChange={(email) =>
              setRegisterForm((current) => ({ ...current, email }))
            }
          />
          <AuthField
            id={`${source}-register-phone`}
            label={model.phoneLabel}
            type="tel"
            value={registerForm.phone}
            autoComplete="tel"
            onChange={(phone) =>
              setRegisterForm((current) => ({ ...current, phone }))
            }
          />
          <AuthField
            id={`${source}-register-password`}
            label={model.passwordLabel}
            type="password"
            value={registerForm.password}
            autoComplete="new-password"
            minLength={8}
            onChange={(password) =>
              setRegisterForm((current) => ({ ...current, password }))
            }
          />
          <AuthCheckbox
            label={model.termsAcceptedLabel}
            checked={registerForm.termsAccepted}
            required
            onChange={(termsAccepted) =>
              setRegisterForm((current) => ({ ...current, termsAccepted }))
            }
          />
          <AuthCheckbox
            label={model.privacyAcceptedLabel}
            checked={registerForm.privacyAccepted}
            required
            onChange={(privacyAccepted) =>
              setRegisterForm((current) => ({ ...current, privacyAccepted }))
            }
          />
          <AuthCheckbox
            label={model.rememberMeLabel}
            checked={registerForm.rememberMe}
            onChange={(rememberMe) =>
              setRegisterForm((current) => ({ ...current, rememberMe }))
            }
          />
          <button className="commerce-action" type="submit" disabled={isMutating}>
            {isMutating ? model.submittingLabel : model.registerAction}
          </button>
        </form>
      )}
    </section>
  );
}

function AuthField({
  id,
  label,
  value,
  onChange,
  type = "text",
  autoComplete,
  minLength,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  type?: string;
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
        autoComplete={autoComplete}
        minLength={minLength}
        required
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function AuthCheckbox({
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
