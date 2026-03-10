"use client";

import { useRef, useState } from "react";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Check, Sparkles, Users, Clock, Shield } from "lucide-react";
import siteConfig from "@/config/site.json";

type ContactModalProps = {
  open: boolean;
  onClose: () => void;
  locale: string;
};

const LABELS = {
  tr: {
    panelTitle: "Projenizi konuşalım",
    panelSub: "Proje fikrinizi veya iş ihtiyacınızı paylaşın, birlikte değerlendirelim.",
    trust: [
      { icon: Sparkles, text: "Kişiselleştirilmiş değerlendirme" },
      { icon: Users, text: "Teknik ekiple birebir görüşme" },
      { icon: Clock, text: "24 saat içinde geri dönüş" },
      { icon: Shield, text: "Ücretsiz, bağlayıcı değil" },
    ],
    stat: "500+ aktif tenant güveniyor",
    formTitle: "İletişime Geç",
    formSub: "Formu doldurun, ekibimiz en kısa sürede size dönüş yapsın.",
    name: "Ad Soyad",
    namePlaceholder: "Ayşe Kaya",
    email: "E-posta",
    emailPlaceholder: "ayse@sirket.com",
    message: "Projeniz veya ihtiyacınız",
    messagePlaceholder: "Proje fikrinizi, ihtiyaçlarınızı veya sorularınızı kısaca anlatın...",
    submit: "Gönder",
    successTitle: "Mesajınız alındı!",
    successSub: "En kısa sürede sizinle iletişime geçeceğiz.",
    successNote: "Spam klasörünüzü de kontrol etmeyi unutmayın.",
    close: "Kapat",
  },
  en: {
    panelTitle: "Let's talk about your project",
    panelSub: "Share your project idea or business need, and we'll explore it together.",
    trust: [
      { icon: Sparkles, text: "Personalized evaluation" },
      { icon: Users, text: "1-on-1 with our technical team" },
      { icon: Clock, text: "Response within 24 hours" },
      { icon: Shield, text: "Free, no commitment" },
    ],
    stat: "500+ active tenants trust us",
    formTitle: "Get in Touch",
    formSub: "Fill out the form and our team will get back to you as soon as possible.",
    name: "Full Name",
    namePlaceholder: "Jane Smith",
    email: "Email",
    emailPlaceholder: "jane@company.com",
    message: "Your project or need",
    messagePlaceholder: "Briefly describe your project idea, needs, or questions...",
    submit: "Send Message",
    successTitle: "Message received!",
    successSub: "We'll get back to you as soon as possible.",
    successNote: "Don't forget to check your spam folder.",
    close: "Close",
  },
};

export function ContactModal({ open, onClose, locale }: ContactModalProps) {
  const l = LABELS[locale as keyof typeof LABELS] ?? LABELS.en;
  const formRef = useRef<HTMLFormElement>(null);
  const [submitted, setSubmitted] = useState(false);

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const name = data.get("name");
    const email = data.get("email");
    const message = data.get("message");
    const subject = encodeURIComponent(`İletişim — ${name}`);
    const body = encodeURIComponent(
      `Ad Soyad: ${name}\nE-posta: ${email}\nMesaj: ${message}`
    );
    window.location.href = `mailto:${siteConfig.contactEmail}?subject=${subject}&body=${body}`;
    setSubmitted(true);
  }

  function handleOpenChange(v: boolean) {
    if (!v) {
      onClose();
      setTimeout(() => setSubmitted(false), 300);
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="gap-0 overflow-hidden p-0 sm:max-w-[640px]">
        <div className="flex min-h-[440px]">
          {/* ── Left branded panel ── */}
          <div className="relative hidden w-[220px] shrink-0 flex-col overflow-hidden p-7 sm:flex" style={{ background: "linear-gradient(160deg, #0d1b35 0%, #0f2044 50%, #111827 100%)" }}>
            <div
              className="pointer-events-none absolute inset-0"
              style={{
                background:
                  "radial-gradient(ellipse 120% 60% at 50% 0%, rgba(37,99,235,0.18) 0%, transparent 65%)",
              }}
            />
            <div
              className="pointer-events-none absolute inset-0 opacity-[0.025]"
              style={{
                backgroundImage: "radial-gradient(circle, #fff 1px, transparent 1px)",
                backgroundSize: "22px 22px",
              }}
            />

            {/* Logo mark */}
            <div className="relative mb-8 flex items-center gap-2">
              <div className="h-6 w-6 rounded-md bg-[var(--color-theme-3)]" />
              <span className="font-heading text-sm font-bold text-white">AdminCraft</span>
            </div>

            {/* Heading + trust */}
            <div className="relative flex-1">
              <h3 className="font-heading text-xl font-extrabold leading-snug text-white">
                {l.panelTitle}
              </h3>
              <p className="mt-2 text-xs leading-relaxed text-white/50">{l.panelSub}</p>

              <ul className="mt-8 flex flex-col gap-4">
                {l.trust.map(({ icon: Icon, text }) => (
                  <li key={text} className="flex items-center gap-3">
                    <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-white/8">
                      <Icon className="h-3.5 w-3.5 text-[var(--color-theme-3)]" />
                    </div>
                    <span className="text-xs font-medium text-white/70">{text}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Bottom social proof */}
            <div className="relative mt-8 rounded-xl border border-white/8 bg-white/5 px-3 py-3">
              <div className="flex items-center gap-2">
                <div className="flex -space-x-1.5">
                  {["#8b8ef8", "#2563EB", "#10b981"].map((c) => (
                    <div
                      key={c}
                      className="h-5 w-5 rounded-full border-2 border-[#0d1b35]"
                      style={{ background: c }}
                    />
                  ))}
                </div>
                <p className="text-[10px] font-semibold text-white/50">{l.stat}</p>
              </div>
            </div>
          </div>

          {/* ── Right panel ── */}
          <div className="flex flex-1 flex-col">
            {submitted ? (
              <div className="flex flex-1 flex-col items-center justify-center gap-4 p-8 text-center">
                <div className="flex h-14 w-14 items-center justify-center rounded-full bg-emerald-100">
                  <Check className="h-7 w-7 text-emerald-600" />
                </div>
                <div>
                  <h3 className="font-heading text-xl font-bold text-[var(--color-dark-neutral-1)]">
                    {l.successTitle}
                  </h3>
                  <p className="mt-2 text-sm text-[var(--color-dark-neutral-2)]">{l.successSub}</p>
                  <p className="mt-1 text-xs text-[var(--color-dark-neutral-2)]/50">{l.successNote}</p>
                </div>
                <Button variant="outline" className="mt-2" onClick={() => handleOpenChange(false)}>
                  {l.close}
                </Button>
              </div>
            ) : (
              <div className="flex flex-1 flex-col p-7 sm:p-8">
                <div className="mb-6">
                  <h2 className="font-heading text-xl font-bold text-[var(--color-dark-neutral-1)]">
                    {l.formTitle}
                  </h2>
                  <p className="mt-1 text-xs text-[var(--color-dark-neutral-2)]">{l.formSub}</p>
                </div>

                <form ref={formRef} onSubmit={handleSubmit} className="flex flex-1 flex-col gap-4">
                  <div className="flex flex-col gap-1.5">
                    <Label htmlFor="contact-name" className="text-xs font-semibold text-[var(--color-dark-neutral-1)]">
                      {l.name}
                    </Label>
                    <Input
                      id="contact-name"
                      name="name"
                      required
                      autoComplete="name"
                      placeholder={l.namePlaceholder}
                      className="h-9 text-sm"
                    />
                  </div>

                  <div className="flex flex-col gap-1.5">
                    <Label htmlFor="contact-email" className="text-xs font-semibold text-[var(--color-dark-neutral-1)]">
                      {l.email}
                    </Label>
                    <Input
                      id="contact-email"
                      name="email"
                      type="email"
                      required
                      autoComplete="email"
                      placeholder={l.emailPlaceholder}
                      className="h-9 text-sm"
                    />
                  </div>

                  <div className="flex flex-col gap-1.5">
                    <Label htmlFor="contact-message" className="text-xs font-semibold text-[var(--color-dark-neutral-1)]">
                      {l.message}
                    </Label>
                    <Textarea
                      id="contact-message"
                      name="message"
                      rows={4}
                      placeholder={l.messagePlaceholder}
                      className="resize-none text-sm"
                    />
                  </div>

                  <Button
                    type="submit"
                    className="mt-auto h-10 w-full font-semibold text-white transition-opacity hover:opacity-90"
                    style={{
                      background: "linear-gradient(135deg, var(--color-theme-3) 0%, #1D4ED8 100%)",
                      boxShadow: "0 4px 16px rgba(37,99,235,0.35)",
                    }}
                  >
                    {l.submit}
                  </Button>
                </form>
              </div>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
