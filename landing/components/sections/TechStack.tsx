"use client";

import { MarqueeTrack } from "@/components/visuals/MarqueeTrack";

/* ── SVG logos ───────────────────────────────────────────────────── */

const SpringIcon = () => (
  <img
    src="/spring-boot.svg"
    alt=""
    className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110"
    loading="lazy"
  />
);

const JavaIcon = () => (
  <svg className="h-5 w-5 shrink-0 transition-all duration-300 opacity-70 group-hover:opacity-100 group-hover:scale-110" viewBox="0 0 256 256" aria-hidden="true">
    <g fill="none" stroke="currentColor" strokeWidth="10" strokeLinecap="round">
      <path d="M96 52c-8 20 12 32 4 48" />
      <path d="M136 44c-10 22 18 36 8 56" />
      <path d="M176 52c-10 20 10 32 4 48" />
    </g>
    <path
      d="M52 120h152v18c0 10-6 16-18 16H70c-12 0-18-6-18-16v-18Z"
      fill="currentColor"
    />
    <path
      d="M70 154l10 62c1 6 6 10 12 10h72c6 0 11-4 12-10l10-62"
      fill="none"
      stroke="currentColor"
      strokeWidth="10"
      strokeLinecap="round"
    />
    <path
      d="M204 144c26 0 40 16 40 32s-14 32-40 32"
      fill="none"
      stroke="currentColor"
      strokeWidth="12"
      strokeLinecap="round"
    />
  </svg>
);

const MySQLIcon = () => (
  <img src="/mysql.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);
const AngularIcon = () => (
  <img src="/angular.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);
const TypeScriptIcon = () => (
  <img src="/typescript.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);
const NextjsIcon = () => (
  <img src="/nextjs.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);
const DockerIcon = () => (
  <img src="/docker.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);
const CloudflareIcon = () => (
  <img src="/cloudflare.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);
const DigitalOceanIcon = () => (
  <img src="/digitalocean.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);
const GitHubIcon = () => (
  <img src="/github.svg" alt="" className="h-5 w-5 shrink-0 transition-all duration-300 grayscale group-hover:grayscale-0 group-hover:scale-110" loading="lazy" />
);

/* ── Data ─────────────────────────────────────────────────────────── */

type Tech = { name: string; icon: React.ReactNode };

const ROW_1: Tech[] = [
  { name: "Angular", icon: <AngularIcon /> },
  { name: "TypeScript", icon: <TypeScriptIcon /> },
  { name: "Next.js", icon: <NextjsIcon /> },
  { name: "Spring Boot", icon: <SpringIcon /> },
  { name: "MySQL", icon: <MySQLIcon /> },
  { name: "Docker", icon: <DockerIcon /> },
  { name: "Cloudflare", icon: <CloudflareIcon /> },
  { name: "DigitalOcean", icon: <DigitalOceanIcon /> },
  { name: "GitHub", icon: <GitHubIcon /> },
];

const ROW_2: Tech[] = [
  { name: "Docker", icon: <DockerIcon /> },
  { name: "Spring Boot", icon: <SpringIcon /> },
  { name: "GitHub", icon: <GitHubIcon /> },
  { name: "Angular", icon: <AngularIcon /> },
  { name: "Cloudflare", icon: <CloudflareIcon /> },
  { name: "MySQL", icon: <MySQLIcon /> },
  { name: "Next.js", icon: <NextjsIcon /> },
  { name: "TypeScript", icon: <TypeScriptIcon /> },
  { name: "DigitalOcean", icon: <DigitalOceanIcon /> },
];

/* ── Component ───────────────────────────────────────────────────── */

function TechItem({ tech }: { tech: Tech }) {
  return (
    <div className="group flex cursor-default items-center gap-2.5 rounded-full border border-neutral-200/60 bg-white/80 px-5 py-2.5 text-neutral-400 shadow-[0_4px_12px_-8px_rgba(15,23,42,0.1)] backdrop-blur-sm transition-[color,border-color,box-shadow,transform] duration-300 hover:-translate-y-0.5 hover:border-neutral-300 hover:text-neutral-700 hover:shadow-[0_8px_20px_-10px_rgba(15,23,42,0.15)]">
      <span className="flex h-5 w-5 items-center justify-center text-current" aria-hidden="true">
        {tech.icon}
      </span>
      <span className="whitespace-nowrap text-[0.72rem] font-semibold tracking-[0.08em] uppercase text-current">
        {tech.name}
      </span>
    </div>
  );
}

type TechStackProps = { label: string };

export function TechStack({ label }: TechStackProps) {
  return (
    <section className="relative overflow-hidden border-y border-neutral-200/80 bg-[linear-gradient(180deg,#f7f8fa_0%,#ffffff_52%,#ffffff_100%)] py-8 sm:py-10">
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage: "radial-gradient(circle, #0f172a 1px, transparent 1px)",
          backgroundSize: "26px 26px",
        }}
        aria-hidden
      />

      <div className="relative mx-auto max-w-[1440px] px-4 sm:px-6 lg:px-20">
        <p className="mb-6 text-center text-[0.66rem] font-semibold uppercase tracking-[0.28em] text-neutral-400">
          {label}
        </p>

        <div className="flex flex-col gap-4">
          <MarqueeTrack direction="left" speed={35}>
            {ROW_1.map((tech) => (
              <TechItem key={tech.name} tech={tech} />
            ))}
          </MarqueeTrack>

          <MarqueeTrack direction="right" speed={40}>
            {ROW_2.map((tech) => (
              <TechItem key={`r2-${tech.name}`} tech={tech} />
            ))}
          </MarqueeTrack>
        </div>
      </div>
    </section>
  );
}
