"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import type { DemoRequestModalContent } from "@/content/home";
import {
  DemoSubmitError,
  fetchPlatformCmsConfig,
  getCraftiveApiOrigin,
  getRecaptchaToken,
  PlatformConfigError,
  submitDemoRequest,
  type PlatformCmsConfig,
} from "@/lib/platform-api";
import {
  motion,
  AnimatePresence,
  useReducedMotion,
} from "framer-motion";
import {
  ArrowRight,
  Check,
  Clock,
  Shield,
  Sparkles,
  Users,
  X,
  type LucideIcon,
} from "lucide-react";
import { useEffect, useState } from "react";

type DemoRequestModalProps = {
  open: boolean;
  onClose: () => void;
  locale: string;
  content: DemoRequestModalContent;
};

const TRUST_ICONS: Record<
  DemoRequestModalContent["trust"][number]["icon"],
  LucideIcon
> = {
  sparkles: Sparkles,
  users: Users,
  clock: Clock,
  shield: Shield,
};

function normalizePhoneForApi(raw: string): string {
  const t = raw.trim();
  if (!t) return "";
  const withPlus = t.startsWith("+");
  const digits = t.replace(/\D/g, "");
  if (!digits) return "";
  return withPlus ? `+${digits}` : digits;
}

function Notice({ tone, message }: { tone: "warning" | "error"; message: string }) {
  const styles =
    tone === "warning"
      ? "border-amber-200/80 bg-amber-50 text-amber-800"
      : "border-red-200/80 bg-red-50 text-red-700";
  return (
    <motion.div
      initial={{ opacity: 0, y: -8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={`rounded-2xl border px-4 py-3 text-sm ${styles}`}
      role={tone === "error" ? "alert" : undefined}
    >
      {message}
    </motion.div>
  );
}

function Field({ id, label, children }: { id: string; label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-2">
      <Label
        htmlFor={id}
        className="text-xs font-semibold tracking-[0.08em] text-[var(--color-dark-neutral-1)]/78 uppercase"
      >
        {label}
      </Label>
      {children}
    </div>
  );
}

const staggerItem = {
  hidden: { opacity: 0, y: 12 },
  visible: { opacity: 1, y: 0 },
};

export function DemoRequestModal({ open, onClose, locale, content }: DemoRequestModalProps) {
  const shouldReduce = useReducedMotion();
  const localeTag = locale === "tr" ? "tr" : "en";
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [cmsConfig, setCmsConfig] = useState<PlatformCmsConfig | null>(null);
  const [configErrorText, setConfigErrorText] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState("");
  const [successFollowUpNote, setSuccessFollowUpNote] = useState("");

  const apiOrigin = getCraftiveApiOrigin();

  useEffect(() => {
    if (!open) return;
    if (!apiOrigin) {
      // Env var eksik — reCAPTCHA gerekmez, butonu aktif et; submit hatayı gösterir
      setCmsConfig({ recaptchaEnabled: false, recaptchaSiteKey: "" });
      return;
    }
    let cancelled = false;
    setConfigErrorText(null);
    fetchPlatformCmsConfig(localeTag)
      .then((cfg) => { if (!cancelled) setCmsConfig(cfg); })
      .catch((err: unknown) => {
        if (!cancelled) {
          setCmsConfig({ recaptchaEnabled: false, recaptchaSiteKey: "" });
          if (PlatformConfigError.is(err)) {
            setConfigErrorText(err.apiMessage ?? content.errConfigFallback);
          } else {
            setConfigErrorText(content.errConfigFallback);
          }
        }
      });
    return () => { cancelled = true; };
  }, [open, apiOrigin, localeTag, content.errConfigFallback]);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setFormError(null);
    if (!apiOrigin) { setFormError(content.errApiNotConfigured); return; }
    const data = new FormData(e.currentTarget);
    const fullName = String(data.get("name") ?? "").trim();
    const email = String(data.get("email") ?? "").trim();
    const phone = normalizePhoneForApi(String(data.get("phone") ?? ""));
    const message = String(data.get("message") ?? "").trim();
    let recaptchaToken: string | null = null;
    if (cmsConfig?.recaptchaEnabled) {
      const siteKey = cmsConfig.recaptchaSiteKey?.trim() ?? "";
      if (!siteKey) { setFormError(content.errRecaptchaClient); return; }
      try { recaptchaToken = await getRecaptchaToken(siteKey); }
      catch { setFormError(content.errRecaptchaClient); return; }
    }

    setSubmitting(true);
    try {
      const result = await submitDemoRequest({ fullName, email, phone, message, locale: localeTag, recaptchaToken }, localeTag);
      setSuccessMessage(result.message);
      setSuccessFollowUpNote(result.followUpNote);
      setSubmitted(true);
    } catch (err) {
      if (DemoSubmitError.is(err)) {
        setFormError(err.apiMessage ?? (err.reason === "network" ? content.errNetwork : content.errHttpNoMessage));
      } else if (err instanceof Error && err.message === "api_not_configured") {
        setFormError(content.errApiNotConfigured);
      } else {
        setFormError(content.errHttpNoMessage);
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
        setSuccessFollowUpNote("");
      }, 300);
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent
        showCloseButton={false}
        className="max-h-[88vh] overflow-hidden border-0 bg-transparent p-0 shadow-none sm:max-w-[760px]"
      >
        <DialogTitle className="sr-only">
          {submitted ? successMessage || content.successBadge : content.formTitle}
        </DialogTitle>
        <DialogDescription className="sr-only">
          {submitted
            ? [successFollowUpNote].filter(Boolean).join(" ") || successMessage
            : `${content.formSub} ${content.panelSub}`}
        </DialogDescription>

        <motion.div
          initial={shouldReduce ? false : { opacity: 0, scale: 0.95, y: 16 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
          className="overflow-hidden rounded-[30px] border border-[#0f172a]/10 bg-white shadow-[0_40px_120px_-44px_rgba(15,23,42,0.45)]"
        >
          <div className="grid lg:grid-cols-[272px_1fr]">
            {/* Left panel */}
            <div
              className="relative overflow-hidden px-5 py-5 sm:px-6 lg:px-5"
              style={{
                background: "linear-gradient(165deg, #08111f 0%, #10234b 46%, #0a1326 100%)",
              }}
            >
              <div
                className="pointer-events-none absolute inset-0"
                style={{
                  background: "radial-gradient(ellipse 110% 60% at 50% 0%, rgba(37,99,235,0.22) 0%, transparent 64%)",
                }}
                aria-hidden="true"
              />
              <div
                className="pointer-events-none absolute inset-0 opacity-[0.04]"
                style={{
                  backgroundImage: "radial-gradient(circle, #fff 1px, transparent 1px)",
                  backgroundSize: "24px 24px",
                }}
                aria-hidden="true"
              />
              {/* Floating orb */}
              <div
                className="pointer-events-none absolute -right-8 top-16 h-32 w-32 rounded-full bg-blue-500/10 blur-2xl animate-float-subtle"
                aria-hidden="true"
              />
              {/* Shimmer line */}
              <div
                className="pointer-events-none absolute inset-x-5 top-0 h-px bg-[linear-gradient(90deg,transparent_0%,rgba(96,165,250,0.5)_50%,transparent_100%)] animate-shimmer-slow"
                aria-hidden="true"
              />

              <div className="relative flex h-full flex-col">
                <motion.div
                  className="flex items-center gap-3"
                  initial={shouldReduce ? false : { opacity: 0, x: -12 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.4, delay: 0.1 }}
                >
                  <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#2563eb_0%,#60a5fa_100%)] shadow-[0_16px_40px_-20px_rgba(37,99,235,0.75)]">
                    <span className="h-3 w-3 rounded-full bg-white/95" aria-hidden="true" />
                  </div>
                  <div>
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-white/45">
                      {content.panelBadge}
                    </p>
                    <p className="font-heading text-lg font-bold text-white">Craftive</p>
                  </div>
                </motion.div>

                <motion.div
                  className="mt-6"
                  initial={shouldReduce ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.4, delay: 0.2 }}
                >
                  <h3 className="font-heading max-w-[12ch] text-[1.85rem] font-bold leading-[1.03] tracking-tight text-white">
                    {content.panelTitle}
                  </h3>
                  <p className="mt-2.5 max-w-[27ch] text-sm leading-5 text-white/62">
                    {content.panelSub}
                  </p>
                </motion.div>

                <motion.div
                  className="mt-5 grid gap-2"
                  initial="hidden"
                  animate="visible"
                  variants={{
                    hidden: {},
                    visible: { transition: { staggerChildren: shouldReduce ? 0 : 0.06, delayChildren: 0.3 } },
                  }}
                >
                  {content.trust.map(({ icon, title }) => {
                    const Icon = TRUST_ICONS[icon];
                    return (
                      <motion.div
                        key={title}
                        variants={shouldReduce ? {} : staggerItem}
                        transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
                        className="rounded-[16px] border border-white/10 bg-white/[0.05] px-3 py-2.5 backdrop-blur-sm transition-[background-color,border-color] duration-200 hover:border-white/20 hover:bg-white/[0.08]"
                      >
                        <div className="flex items-center gap-3">
                          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-white/[0.06]">
                            <Icon className="h-3.5 w-3.5 text-[#60a5fa]" />
                          </div>
                          <p className="text-sm font-semibold leading-5 text-white/84">{title}</p>
                        </div>
                      </motion.div>
                    );
                  })}
                </motion.div>
              </div>
            </div>

            {/* Right panel */}
            <div className="relative flex flex-col overflow-y-auto bg-[linear-gradient(180deg,#ffffff_0%,#fbfcff_100%)]">
              <DialogClose asChild>
                <button
                  type="button"
                  className="absolute right-5 top-5 flex h-10 w-10 items-center justify-center rounded-full border border-neutral-200 bg-white text-neutral-500 shadow-[0_14px_24px_-20px_rgba(15,23,42,0.35)] transition-[color,border-color,box-shadow,transform] duration-200 hover:border-neutral-300 hover:text-neutral-900 hover:scale-105 active:scale-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-neutral-300"
                  aria-label={content.close}
                >
                  <X className="h-4.5 w-4.5" />
                </button>
              </DialogClose>

              <AnimatePresence mode="wait">
                {submitted ? (
                  <motion.div
                    key="success"
                    initial={shouldReduce ? false : { opacity: 0, scale: 0.96, y: 12 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.96 }}
                    transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
                    className="flex items-center px-6 py-7 sm:px-7"
                  >
                    <div className="w-full rounded-[24px] border border-emerald-100 bg-[linear-gradient(180deg,#ffffff_0%,#f7fffb_100%)] p-6 shadow-[0_34px_80px_-52px_rgba(16,185,129,0.28)]">
                      <div className="flex flex-col items-start gap-6 text-left">
                        <motion.div
                          initial={shouldReduce ? false : { scale: 0 }}
                          animate={{ scale: 1 }}
                          transition={{ type: "spring", stiffness: 300, damping: 15, delay: 0.15 }}
                          className="inline-flex items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs font-semibold text-emerald-700"
                        >
                          <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-emerald-600 text-white">
                            <Check className="h-3.5 w-3.5" />
                          </span>
                          {content.successBadge}
                        </motion.div>

                        <div className="space-y-3">
                          <h3 className="font-heading text-3xl font-bold tracking-tight text-[var(--color-dark-neutral-1)]">
                            {successMessage || content.successBadge}
                          </h3>
                          {successFollowUpNote ? (
                            <p className="max-w-[44ch] text-sm leading-7 text-[var(--color-dark-neutral-2)]/72">
                              {successFollowUpNote}
                            </p>
                          ) : null}
                        </div>

                        <Button
                          variant="outline"
                          className="rounded-full border-neutral-200 bg-white px-5"
                          onClick={() => handleOpenChange(false)}
                        >
                          {content.close}
                        </Button>
                      </div>
                    </div>
                  </motion.div>
                ) : (
                  <motion.div
                    key="form"
                    initial={shouldReduce ? false : { opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.25 }}
                    className="flex flex-col px-6 py-6 sm:px-7 sm:py-6"
                  >
                    <motion.div
                      className="max-w-[480px]"
                      initial={shouldReduce ? false : { opacity: 0, y: 12 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.4, delay: 0.1 }}
                    >
                      <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-[var(--color-dark-neutral-2)]/45">
                        {content.formEyebrow}
                      </p>
                      <h2 className="mt-2.5 font-heading text-[1.85rem] font-bold tracking-tight text-[var(--color-dark-neutral-1)]">
                        {content.formTitle}
                      </h2>
                      <p className="mt-2.5 text-sm leading-6 text-[var(--color-dark-neutral-2)]">
                        {content.formSub}
                      </p>
                    </motion.div>

                    <div className="mt-6 space-y-3">
                      {configErrorText ? <Notice tone="warning" message={configErrorText} /> : null}
                      {formError ? <Notice tone="error" message={formError} /> : null}
                    </div>

                    <motion.form
                      onSubmit={(ev) => void handleSubmit(ev)}
                      className="mt-6 flex flex-col gap-3.5"
                      initial="hidden"
                      animate="visible"
                      variants={{
                        hidden: {},
                        visible: { transition: { staggerChildren: shouldReduce ? 0 : 0.05, delayChildren: 0.2 } },
                      }}
                    >
                      <motion.div
                        variants={shouldReduce ? {} : staggerItem}
                        transition={{ duration: 0.35 }}
                        className="grid gap-3.5 sm:grid-cols-2"
                      >
                        <Field id="contact-name" label={content.name}>
                          <Input
                            id="contact-name"
                            name="name"
                            required
                            disabled={submitting}
                            autoComplete="name"
                            maxLength={255}
                            placeholder={content.namePlaceholder}
                            className="h-11 rounded-2xl border-neutral-200 bg-white px-4 shadow-none focus-visible:border-[#60a5fa] focus-visible:ring-[#60a5fa]/20"
                          />
                        </Field>

                        <Field id="contact-email" label={content.email}>
                          <Input
                            id="contact-email"
                            name="email"
                            type="email"
                            required
                            disabled={submitting}
                            autoComplete="email"
                            maxLength={255}
                            placeholder={content.emailPlaceholder}
                            className="h-11 rounded-2xl border-neutral-200 bg-white px-4 shadow-none focus-visible:border-[#60a5fa] focus-visible:ring-[#60a5fa]/20"
                          />
                        </Field>
                      </motion.div>

                      <motion.div variants={shouldReduce ? {} : staggerItem} transition={{ duration: 0.35 }}>
                        <Field id="contact-phone" label={content.phone}>
                          <Input
                            id="contact-phone"
                            name="phone"
                            type="tel"
                            inputMode="tel"
                            autoComplete="tel"
                            disabled={submitting}
                            maxLength={40}
                            placeholder={content.phonePlaceholder}
                            className="h-11 w-full rounded-2xl border-neutral-200 bg-white px-4 shadow-none focus-visible:border-[#60a5fa] focus-visible:ring-[#60a5fa]/20"
                          />
                        </Field>
                      </motion.div>

                      <motion.div variants={shouldReduce ? {} : staggerItem} transition={{ duration: 0.35 }}>
                        <Field id="contact-message" label={content.message}>
                          <Textarea
                            id="contact-message"
                            name="message"
                            required
                            disabled={submitting}
                            rows={6}
                            maxLength={5000}
                            placeholder={content.messagePlaceholder}
                            className="min-h-[132px] rounded-[22px] border-neutral-200 bg-white px-4 py-3 text-sm shadow-none focus-visible:border-[#60a5fa] focus-visible:ring-[#60a5fa]/20 resize-none"
                          />
                        </Field>
                      </motion.div>

                      <motion.div
                        variants={shouldReduce ? {} : staggerItem}
                        transition={{ duration: 0.35 }}
                        className="rounded-[20px] border border-neutral-200/80 bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] p-3"
                      >
                        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                          <p className="text-sm text-[var(--color-dark-neutral-2)]/72">
                            {content.submitHint}
                          </p>
                          <Button
                            type="submit"
                            disabled={submitting || (cmsConfig === null && configErrorText === null)}
                            className="group h-12 rounded-full px-6 font-semibold text-white transition-[opacity,box-shadow,transform] duration-200 hover:opacity-95 hover:shadow-[0_20px_36px_-20px_rgba(37,99,235,0.5)] hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60"
                            style={{
                              background: "linear-gradient(135deg, var(--color-theme-3) 0%, #1D4ED8 100%)",
                              boxShadow: "0 18px 34px -20px rgba(37,99,235,0.55)",
                            }}
                          >
                            {submitting ? content.submitting : content.submit}
                            <ArrowRight className="h-4 w-4 transition-transform duration-200 group-hover:translate-x-0.5" />
                          </Button>
                        </div>
                      </motion.div>
                    </motion.form>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </div>
        </motion.div>
      </DialogContent>
    </Dialog>
  );
}
