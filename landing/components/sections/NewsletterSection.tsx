"use client";

import { useRef, useState } from "react";
import { AnimateInView } from "@/components/AnimateInView";
import {
  NewsletterSubscribeError,
  subscribePlatformNewsletter,
} from "@/lib/platform-api";
import { ArrowRight, Check, Mail } from "lucide-react";

type NewsletterContent = {
  eyebrow: string;
  heading: string;
  subheading: string;
  placeholder: string;
  submit: string;
  submitting: string;
  errNetwork: string;
  errHttp: string;
};

type State = "idle" | "submitting" | "success" | "error";

export function NewsletterSection({
  content,
  locale,
}: {
  content: NewsletterContent;
  locale: string;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const formStartedAtRef = useRef<number>(Date.now());
  const [state, setState] = useState<State>("idle");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string>("");

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const email = inputRef.current?.value.trim() ?? "";
    const honeypot = String(data.get("website") ?? "").trim();
    if (!email) return;

    setState("submitting");
    setErrorMsg(null);
    setSuccessMsg("");

    try {
      const result = await subscribePlatformNewsletter(
        email,
        locale,
        honeypot,
        formStartedAtRef.current,
      );
      setSuccessMsg(result.message);
      setState("success");
    } catch (err) {
      if (NewsletterSubscribeError.is(err)) {
        setErrorMsg(
          err.apiMessage ??
            (err.reason === "network" ? content.errNetwork : content.errHttp),
        );
      } else {
        setErrorMsg(content.errHttp);
      }
      setState("error");
    }
  }

  return (
    <section className="relative overflow-hidden bg-[var(--color-light-neutral-1)] px-4 py-20 sm:px-6 lg:px-20">
      {/* subtle grid */}
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage:
            "linear-gradient(var(--color-shade) 1px, transparent 1px), linear-gradient(90deg, var(--color-shade) 1px, transparent 1px)",
          backgroundSize: "40px 40px",
        }}
        aria-hidden
      />

      <div className="relative mx-auto max-w-[720px] text-center">
        <AnimateInView>
          <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-[var(--color-shade)] bg-white px-4 py-1.5 text-xs font-semibold uppercase tracking-widest text-[var(--color-dark-neutral-2)]">
            <Mail className="h-3.5 w-3.5 text-[var(--color-theme-3)]" />
            {content.eyebrow}
          </div>
        </AnimateInView>

        <AnimateInView delay={1}>
          <h2 className="font-display mt-3 text-3xl text-[var(--color-dark-neutral-1)] sm:text-4xl">
            {content.heading}
          </h2>
        </AnimateInView>

        <AnimateInView delay={2}>
          <p className="mt-3 text-[var(--color-dark-neutral-2)]">
            {content.subheading}
          </p>
        </AnimateInView>

        <AnimateInView delay={3} className="mt-8">
          {state === "success" ? (
            <div className="inline-flex items-center gap-2.5 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-3.5 text-sm font-medium text-emerald-700">
              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-600 text-white">
                <Check className="h-3 w-3" />
              </span>
              {successMsg}
            </div>
          ) : (
            <form
              onSubmit={(ev) => void handleSubmit(ev)}
              className="flex flex-col items-center gap-3 sm:flex-row sm:justify-center"
            >
              <div
                aria-hidden="true"
                className="pointer-events-none absolute left-[-9999px] top-auto h-px w-px overflow-hidden opacity-0"
              >
                <label htmlFor="newsletter-website">Website</label>
                <input
                  id="newsletter-website"
                  name="website"
                  type="text"
                  tabIndex={-1}
                  autoComplete="off"
                  defaultValue=""
                />
              </div>
              <input
                ref={inputRef}
                type="email"
                required
                disabled={state === "submitting"}
                placeholder={content.placeholder}
                className="h-12 w-full max-w-sm rounded-2xl border border-[var(--color-shade)] bg-white px-4 text-sm text-[var(--color-dark-neutral-1)] placeholder:text-[var(--color-dark-neutral-2)]/50 focus:border-[var(--color-theme-3)] focus:outline-none focus:ring-2 focus:ring-[var(--color-theme-3)]/20 disabled:opacity-60 sm:w-72"
              />
              <button
                type="submit"
                disabled={state === "submitting"}
                className="group inline-flex h-12 items-center gap-2 rounded-2xl bg-[var(--color-theme-3)] px-6 text-sm font-semibold text-white transition-all hover:opacity-90 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60"
              >
                {state === "submitting" ? content.submitting : content.submit}
                {state !== "submitting" && (
                  <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />
                )}
              </button>
            </form>
          )}

          {state === "error" && errorMsg && (
            <p className="mt-3 text-sm text-red-600">{errorMsg}</p>
          )}
        </AnimateInView>
      </div>
    </section>
  );
}
