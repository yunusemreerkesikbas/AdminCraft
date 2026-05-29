"use client";

import { Dialog } from "@base-ui/react/dialog";
import { useEffect, useState } from "react";
import { useTranslations } from "next-intl";
import { XIcon, CheckIcon, ArrowRightIcon } from "@/components/icons";
import {
  DemoSubmitError,
  fetchPlatformCmsConfig,
  getCraftiveApiOrigin,
  getRecaptchaToken,
  PlatformConfigError,
  submitDemoRequest,
  type PlatformCmsConfig,
} from "@/lib/platform-api";

type Props = { open: boolean; onClose: () => void; locale: string };

function normalizePhone(raw: string): string {
  const t = raw.trim();
  if (!t) return "";
  const withPlus = t.startsWith("+");
  const digits = t.replace(/\D/g, "");
  if (!digits) return "";
  return withPlus ? `+${digits}` : digits;
}

function FieldGroup({
  num,
  label,
  children,
}: {
  num: string;
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <p className="text-[9px] font-black tracking-[0.28em] uppercase mb-2.5" style={{ color: "#a09a90" }}>
        <span style={{ color: "#0E121B" }}>{num}</span>
        {" — "}
        {label}
      </p>
      {children}
    </div>
  );
}

const lineInput =
  "w-full bg-transparent border-0 border-b border-b-[#ccc8c0] pb-2.5 pt-0.5 " +
  "text-[15px] leading-snug text-[#1a1a1a] placeholder:text-[#bbb6ad] " +
  "focus:outline-none focus:border-b-[#0E121B] " +
  "transition-colors duration-200 disabled:opacity-40";

const lineTextarea =
  "w-full bg-transparent border-0 border-b border-b-[#ccc8c0] pb-2.5 pt-0.5 " +
  "text-[15px] leading-relaxed text-[#1a1a1a] placeholder:text-[#bbb6ad] " +
  "focus:outline-none focus:border-b-[#0E121B] " +
  "transition-colors duration-200 resize-none disabled:opacity-40";

export function DemoRequestModal({ open, onClose, locale }: Props) {
  const t = useTranslations("demoModal");
  const localeTag = locale === "tr" ? "tr" : "en";

  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [configError, setConfigError] = useState<string | null>(null);
  const [cmsConfig, setCmsConfig] = useState<PlatformCmsConfig | null>(null);
  const [successMessage, setSuccessMessage] = useState("");
  const [successNote, setSuccessNote] = useState("");

  const apiOrigin = getCraftiveApiOrigin();

  useEffect(() => {
    if (!open) return;
    if (!apiOrigin) {
      setCmsConfig({ recaptchaEnabled: false, recaptchaSiteKey: "" });
      return;
    }
    let cancelled = false;
    setConfigError(null);
    fetchPlatformCmsConfig(localeTag)
      .then((cfg) => { if (!cancelled) setCmsConfig(cfg); })
      .catch((err: unknown) => {
        if (!cancelled) {
          setCmsConfig({ recaptchaEnabled: false, recaptchaSiteKey: "" });
          setConfigError(
            PlatformConfigError.is(err) ? (err.apiMessage ?? t("errConfig")) : t("errConfig"),
          );
        }
      });
    return () => { cancelled = true; };
  }, [open, apiOrigin, localeTag, t]);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);
    if (!apiOrigin) { setFormError(t("errApiConfig")); return; }
    const fd = new FormData(e.currentTarget);
    const fullName = String(fd.get("name") ?? "").trim();
    const email = String(fd.get("email") ?? "").trim();
    const phone = normalizePhone(String(fd.get("phone") ?? ""));
    const message = String(fd.get("message") ?? "").trim();

    const honeypot = String(fd.get("website") ?? "").trim();
    if (honeypot) return;

    let recaptchaToken: string | null = null;
    if (cmsConfig?.recaptchaEnabled) {
      const siteKey = cmsConfig.recaptchaSiteKey?.trim() ?? "";
      if (!siteKey) { setFormError(t("errRecaptcha")); return; }
      try { recaptchaToken = await getRecaptchaToken(siteKey); }
      catch { setFormError(t("errRecaptcha")); return; }
    }

    setSubmitting(true);
    try {
      const result = await submitDemoRequest(
        { fullName, email, phone, message, locale: localeTag, recaptchaToken },
        localeTag,
      );
      setSuccessMessage(result.message);
      setSuccessNote(result.followUpNote);
      setSubmitted(true);
    } catch (err) {
      if (DemoSubmitError.is(err)) {
        setFormError(err.apiMessage ?? (err.reason === "network" ? t("errNetwork") : t("errHttp")));
      } else if (err instanceof Error && err.message === "api_not_configured") {
        setFormError(t("errApiConfig"));
      } else {
        setFormError(t("errHttp"));
      }
    } finally {
      setSubmitting(false);
    }
  }

  function handleOpenChange(v: boolean) {
    if (!v) {
      onClose();
      setTimeout(() => {
        setSubmitted(false);
        setFormError(null);
        setSuccessMessage("");
        setSuccessNote("");
      }, 350);
    }
  }

  return (
    <Dialog.Root open={open} onOpenChange={handleOpenChange}>
      <Dialog.Portal>
        {/* Backdrop */}
        <Dialog.Backdrop
          className="fixed inset-0 z-[90]
                     transition-opacity duration-300 ease-out
                     starting:opacity-0 data-[closed]:opacity-0"
          style={{ background: "rgba(8,8,8,0.72)", backdropFilter: "blur(5px)" }}
        />

        {/* Popup */}
        <Dialog.Popup
          className="fixed inset-0 z-[90] flex items-end sm:items-center justify-center p-0 sm:p-6
                     outline-none
                     transition-opacity duration-300 ease-out
                     starting:opacity-0 data-[closed]:opacity-0"
        >
          {/* Card */}
          <div
            className="relative w-full sm:max-w-[560px] max-h-[100dvh] sm:max-h-[92vh]
                       overflow-y-auto overflow-x-hidden
                       rounded-t-[22px] sm:rounded-[22px]
                       transition-transform duration-350 ease-out
                       starting:translate-y-8 sm:starting:translate-y-4"
            style={{
              background: "#f8f6f2",
              borderTop: "3px solid #0E121B",
              boxShadow: "0 48px_120px_-32px rgba(0,0,0,0.5)",
            }}
          >
            {/* Ghost "DEMO" background decoration */}
            <div
              aria-hidden
              className="pointer-events-none select-none absolute top-0 right-5 font-heading font-black leading-none tracking-[-0.04em]"
              style={{
                fontSize: "clamp(80px, 20vw, 148px)",
                color: "#1a1a1a",
                opacity: 0.04,
                lineHeight: 1,
                paddingTop: "12px",
              }}
            >
              DEMO
            </div>

            {submitted ? (
              /* ─── Success state ─── */
              <div className="relative px-7 py-9 sm:px-9 flex flex-col items-start gap-6">
                {/* Orange circle check */}
                <div
                  className="flex items-center justify-center rounded-full"
                  style={{ width: 52, height: 52, background: "#0E121B" }}
                >
                  <CheckIcon size={22} className="text-white" strokeWidth={2.5} />
                </div>

                <div>
                  <p className="text-[9px] font-black tracking-[0.28em] uppercase mb-3" style={{ color: "#0E121B" }}>
                    {t("successBadge")}
                  </p>
                  <h2
                    className="font-heading font-bold leading-[1.05] tracking-tight"
                    style={{ fontSize: "clamp(28px, 5vw, 38px)", color: "#1a1a1a" }}
                  >
                    {successMessage || t("successTitle")}
                  </h2>
                  {successNote && (
                    <p className="mt-3 text-[14px] leading-relaxed max-w-[38ch]" style={{ color: "#7a746c" }}>
                      {successNote}
                    </p>
                  )}
                </div>

                <button
                  onClick={() => handleOpenChange(false)}
                  className="inline-flex items-center gap-2 text-[13px] font-medium border-b border-b-[#ccc8c0] pb-0.5 hover:border-b-[#0E121B] transition-colors duration-150"
                  style={{ color: "#5a544e" }}
                >
                  {t("close")}
                </button>
              </div>
            ) : (
              /* ─── Form state ─── */
              <div className="relative px-7 py-7 sm:px-9 sm:py-8">
                {/* Header row */}
                <div className="flex items-start justify-between mb-1">
                  <div>
                    <p
                      className="text-[9px] font-black tracking-[0.28em] uppercase mb-2.5"
                      style={{ color: "#0E121B" }}
                    >
                      {t("formEyebrow")}
                    </p>
                    <h2
                      className="font-heading font-bold leading-[1.05] tracking-tight"
                      style={{ fontSize: "clamp(26px, 5vw, 36px)", color: "#1a1a1a" }}
                    >
                      {t("formTitle")}
                    </h2>
                    <p className="mt-1.5 text-[14px] leading-relaxed" style={{ color: "#8a847c" }}>
                      {t("formSub")}
                    </p>
                  </div>

                  {/* Close button */}
                  <Dialog.Close
                    className="flex-shrink-0 ml-4 mt-0.5 size-9 rounded-full flex items-center justify-center
                               transition-colors duration-150 hover:bg-[#ede9e3]"
                    style={{ color: "#8a847c" }}
                    aria-label={t("close")}
                  >
                    <XIcon size={14} />
                  </Dialog.Close>
                </div>

                {/* Thin divider */}
                <div className="mt-6 mb-6 h-px" style={{ background: "#e4e0d8" }} />

                {/* Notices */}
                {(configError ?? formError) && (
                  <div className="mb-5 space-y-2">
                    {configError && (
                      <div
                        className="rounded-xl px-4 py-3 text-[13px]"
                        style={{ background: "#fef9ec", color: "#8a6700" }}
                      >
                        {configError}
                      </div>
                    )}
                    {formError && (
                      <div
                        role="alert"
                        className="rounded-xl px-4 py-3 text-[13px]"
                        style={{ background: "#fff2f2", color: "#b32020" }}
                      >
                        {formError}
                      </div>
                    )}
                  </div>
                )}

                <form onSubmit={(e) => void handleSubmit(e)} className="space-y-6">
                  {/* Honeypot — invisible bot trap */}
                  <input
                    name="website"
                    tabIndex={-1}
                    autoComplete="off"
                    aria-hidden="true"
                    style={{ position: "absolute", left: "-9999px", width: "1px", height: "1px", overflow: "hidden" }}
                  />
                  {/* Name + Email */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                    <FieldGroup num="01" label={t("name")}>
                      <input
                        id="dm-name"
                        name="name"
                        required
                        disabled={submitting}
                        autoComplete="name"
                        maxLength={255}
                        placeholder={t("namePlaceholder")}
                        className={lineInput}
                      />
                    </FieldGroup>
                    <FieldGroup num="02" label={t("email")}>
                      <input
                        id="dm-email"
                        name="email"
                        type="email"
                        required
                        disabled={submitting}
                        autoComplete="email"
                        maxLength={255}
                        placeholder={t("emailPlaceholder")}
                        className={lineInput}
                      />
                    </FieldGroup>
                  </div>

                  {/* Phone */}
                  <FieldGroup num="03" label={t("phone")}>
                    <input
                      id="dm-phone"
                      name="phone"
                      type="tel"
                      inputMode="tel"
                      autoComplete="tel"
                      disabled={submitting}
                      maxLength={40}
                      placeholder={t("phonePlaceholder")}
                      className={lineInput}
                    />
                  </FieldGroup>

                  {/* Message */}
                  <FieldGroup num="04" label={t("message")}>
                    <textarea
                      id="dm-message"
                      name="message"
                      required
                      disabled={submitting}
                      rows={3}
                      maxLength={5000}
                      placeholder={t("messagePlaceholder")}
                      className={lineTextarea}
                    />
                  </FieldGroup>

                  {/* Submit */}
                  <div className="pt-2">
                    <div className="flex items-center justify-between gap-4">
                      <p className="text-[11px] leading-snug max-w-[26ch]" style={{ color: "#a09a90" }}>
                        {t("submitHint")}
                      </p>
                      <button
                        type="submit"
                        disabled={submitting || cmsConfig === null || configError !== null}
                        className="group flex-shrink-0 inline-flex items-center gap-2.5 h-12 px-6 rounded-full
                                   text-white text-[14px] font-semibold tracking-[0.01em]
                                   disabled:opacity-40 disabled:pointer-events-none
                                   active:scale-[0.98] transition-[background-color,transform,box-shadow] duration-150"
                        style={{
                          background: "#0E121B",
                          boxShadow: "none",
                        }}
                        onMouseEnter={(e) => (e.currentTarget.style.background = "#1e2433")}
                        onMouseLeave={(e) => (e.currentTarget.style.background = "#0E121B")}
                      >
                        {submitting ? t("submitting") : t("submit")}
                        <ArrowRightIcon
                          size={14}
                          className="transition-transform duration-150 group-hover:translate-x-0.5"
                        />
                      </button>
                    </div>
                  </div>
                </form>
              </div>
            )}
          </div>
        </Dialog.Popup>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
