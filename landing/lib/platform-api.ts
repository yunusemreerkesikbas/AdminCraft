const RECAPTCHA_ACTION = "landing_demo_request";

export type PlatformCmsConfig = {
  recaptchaEnabled: boolean;
  recaptchaSiteKey: string;
};

export type ApiEnvelope<T> = {
  result: string;
  message: string;
  data: T;
  code?: number | null;
};

export type DemoSubmitResult = {
  message: string;
  followUpNote: string;
};

export type DemoSubmitErrorReason = "network" | "http";

export class DemoSubmitError extends Error {
  readonly reason: DemoSubmitErrorReason;
  readonly status: number;
  readonly apiMessage: string | null;

  constructor(reason: DemoSubmitErrorReason, status: number, apiMessage: string | null) {
    super(apiMessage ?? reason);
    this.name = "DemoSubmitError";
    this.reason = reason;
    this.status = status;
    this.apiMessage = apiMessage;
  }

  static is(e: unknown): e is DemoSubmitError {
    return e instanceof DemoSubmitError;
  }
}

export class PlatformConfigError extends Error {
  readonly status: number;
  readonly apiMessage: string | null;

  constructor(status: number, apiMessage: string | null) {
    super(apiMessage ?? `config_http_${status}`);
    this.name = "PlatformConfigError";
    this.status = status;
    this.apiMessage = apiMessage;
  }

  static is(e: unknown): e is PlatformConfigError {
    return e instanceof PlatformConfigError;
  }
}

function platformJsonHeaders(acceptLanguage: string): HeadersInit {
  const lang = acceptLanguage.trim() || "en";
  return {
    Accept: "application/json",
    "Accept-Language": lang,
    "Content-Type": "application/json",
  };
}

function normalizeBase(raw: string | undefined): string {
  if (!raw || raw.trim() === "") {
    return "";
  }
  return raw.replace(/\/+$/, "");
}

export function getCraftiveApiOrigin(): string {
  const fromEnv = normalizeBase(process.env.NEXT_PUBLIC_CRAFTIVE_API_URL);
  if (fromEnv) {
    return fromEnv;
  }
  if (process.env.NODE_ENV === "development") {
    return "http://localhost:8080";
  }
  return "";
}

export function craftiveApiUrl(path: string): string {
  const origin = getCraftiveApiOrigin();
  const p = path.startsWith("/") ? path : `/${path}`;
  if (!origin) {
    return p;
  }
  return `${origin}/api${p}`;
}

async function readApiEnvelope(res: Response): Promise<ApiEnvelope<unknown> | null> {
  const ct = res.headers.get("content-type") ?? "";
  if (ct && !ct.includes("application/json")) {
    return null;
  }
  const text = await res.text();
  if (!text.trim()) {
    return null;
  }
  try {
    const parsed: unknown = JSON.parse(text);
    if (
      parsed !== null &&
      typeof parsed === "object" &&
      "result" in parsed &&
      typeof (parsed as ApiEnvelope<unknown>).result === "string"
    ) {
      return parsed as ApiEnvelope<unknown>;
    }
    return null;
  } catch {
    return null;
  }
}

function classifyDemoSubmitFailure(status: number, apiMessage: string | null): DemoSubmitError {
  const msg = apiMessage?.trim() || null;
  return new DemoSubmitError("http", status, msg);
}

export async function fetchPlatformCmsConfig(acceptLanguage: string): Promise<PlatformCmsConfig> {
  const res = await fetch(craftiveApiUrl("/platform/cms/config"), {
    method: "GET",
    headers: {
      Accept: "application/json",
      "Accept-Language": acceptLanguage.trim() || "en",
    },
  });
  const body = await readApiEnvelope(res);
  if (!res.ok || !body) {
    throw new PlatformConfigError(res.status, body?.message?.trim() || null);
  }
  if (body.result !== "SUCCESS" || !body.data) {
    throw new PlatformConfigError(res.status, body.message?.trim() || null);
  }
  const data = body.data as Record<string, string>;
  const enabled = data["security.recaptcha.enabled"] === "true";
  const siteKey = data["security.recaptcha.site_key"] ?? "";
  return { recaptchaEnabled: enabled, recaptchaSiteKey: siteKey };
}

declare global {
  interface Window {
    grecaptcha?: {
      ready: (cb: () => void) => void;
      execute: (siteKey: string, opts: { action: string }) => Promise<string>;
    };
  }
}

export async function getRecaptchaToken(siteKey: string): Promise<string> {
  await loadRecaptchaScript(siteKey);
  return new Promise((resolve, reject) => {
    const g = window.grecaptcha;
    if (!g) {
      reject(new Error("recaptcha_unavailable"));
      return;
    }
    g.ready(() => {
      g.execute(siteKey, { action: RECAPTCHA_ACTION })
        .then(resolve)
        .catch(reject);
    });
  });
}

function loadRecaptchaScript(siteKey: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>(
      "script[data-craftive-recaptcha=\"1\"]",
    );
    if (existing) {
      resolve();
      return;
    }
    const s = document.createElement("script");
    s.src = `https://www.google.com/recaptcha/api.js?render=${encodeURIComponent(siteKey)}`;
    s.async = true;
    s.dataset.craftiveRecaptcha = "1";
    s.onload = () => resolve();
    s.onerror = () => reject(new Error("recaptcha_script"));
    document.head.appendChild(s);
  });
}

export type NewsletterSubscribeErrorReason = "network" | "http";

export class NewsletterSubscribeError extends Error {
  readonly reason: NewsletterSubscribeErrorReason;
  readonly status: number;
  readonly apiMessage: string | null;

  constructor(reason: NewsletterSubscribeErrorReason, status: number, apiMessage: string | null) {
    super(apiMessage ?? reason);
    this.name = "NewsletterSubscribeError";
    this.reason = reason;
    this.status = status;
    this.apiMessage = apiMessage;
  }

  static is(e: unknown): e is NewsletterSubscribeError {
    return e instanceof NewsletterSubscribeError;
  }
}

export type NewsletterSubscribeResult = {
  message: string;
};

export async function subscribePlatformNewsletter(
  email: string,
  locale: string,
  honeypot: string,
  formStartedAt: number,
): Promise<NewsletterSubscribeResult> {
  const origin = getCraftiveApiOrigin();
  if (!origin) {
    throw new NewsletterSubscribeError("network", 0, null);
  }
  const lang = locale.trim() || "en";
  let res: Response;
  try {
    res = await fetch(craftiveApiUrl("/platform/public/newsletter/subscribe"), {
      method: "POST",
      headers: platformJsonHeaders(lang),
      body: JSON.stringify({
        email,
        templateType: "NEWSLETTER_DEFAULT",
        source: "LANDING_NEWSLETTER",
        locale,
        honeypot,
        formStartedAt,
      }),
    });
  } catch {
    throw new NewsletterSubscribeError("network", 0, null);
  }

  const body = await readApiEnvelope(res);

  if (res.ok && body?.result === "SUCCESS") {
    return { message: (body.message ?? "").trim() };
  }

  throw new NewsletterSubscribeError("http", res.status, body?.message?.trim() ?? null);
}

export type DemoRequestPayload = {
  fullName: string;
  email: string;
  phone: string;
  message: string;
  locale: string;
  recaptchaToken: string | null;
};

export async function submitDemoRequest(
  payload: DemoRequestPayload,
  acceptLanguage: string,
): Promise<DemoSubmitResult> {
  const origin = getCraftiveApiOrigin();
  if (!origin) {
    throw new Error("api_not_configured");
  }
  const lang = acceptLanguage.trim() || payload.locale || "en";
  let res: Response;
  try {
    res = await fetch(craftiveApiUrl("/platform/public/demo-requests"), {
      method: "POST",
      headers: platformJsonHeaders(lang),
      body: JSON.stringify({
        fullName: payload.fullName,
        email: payload.email,
        phone: payload.phone,
        message: payload.message,
        locale: payload.locale,
        recaptchaToken: payload.recaptchaToken ?? "",
      }),
    });
  } catch {
    throw new DemoSubmitError("network", 0, null);
  }

  const body = await readApiEnvelope(res);

  if (res.ok && body?.result === "SUCCESS") {
    const message = (body.message ?? "").trim();
    const rawData = body.data;
    let followUpNote = "";
    if (rawData !== null && rawData !== undefined && typeof rawData === "object") {
      const v = (rawData as Record<string, unknown>)["followUpNote"];
      if (typeof v === "string") {
        followUpNote = v.trim();
      }
    }
    return { message, followUpNote };
  }

  if (!body) {
    throw classifyDemoSubmitFailure(res.status, null);
  }

  throw classifyDemoSubmitFailure(res.status, body.message ?? null);
}
