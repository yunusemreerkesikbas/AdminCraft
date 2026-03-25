"use client";

/* ── SVG logos ───────────────────────────────────────────────────── */

const SpringIcon = () => (
  <img
    src="/spring-boot.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const JavaIcon = () => (
  <svg className="h-5 w-5 shrink-0 opacity-70" viewBox="0 0 256 256" aria-hidden="true">
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
  <img
    src="/mysql.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const AngularIcon = () => (
  <img
    src="/angular.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const TypeScriptIcon = () => (
  <img
    src="/typescript.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const NextjsIcon = () => (
  <img
    src="/nextjs.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const DockerIcon = () => (
  <img
    src="/docker.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const CloudflareIcon = () => (
  <img
    src="/cloudflare.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const DigitalOceanIcon = () => (
  <img
    src="/digitalocean.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

const GitHubIcon = () => (
  <img
    src="/github.svg"
    alt=""
    className="h-5 w-5 shrink-0 opacity-70 grayscale"
    loading="lazy"
  />
);

/* ── Data ─────────────────────────────────────────────────────────── */

type Tech = { name: string; icon: React.ReactNode };

const STACK: Tech[] = [
  { name: "Angular",      icon: <AngularIcon /> },
  { name: "TypeScript",   icon: <TypeScriptIcon /> },
  { name: "Next.js",      icon: <NextjsIcon /> },
  { name: "Spring Boot",  icon: <SpringIcon /> },
  { name: "MySQL",        icon: <MySQLIcon /> },
  { name: "Docker",       icon: <DockerIcon /> },
  { name: "Cloudflare",   icon: <CloudflareIcon /> },
  { name: "DigitalOcean", icon: <DigitalOceanIcon /> },
  { name: "GitHub",       icon: <GitHubIcon /> },
];

/* ── Component ───────────────────────────────────────────────────── */

function TechItem({ tech }: { tech: Tech }) {
  return (
    <div className="group flex cursor-default items-center gap-2.5 px-1.5 py-2 text-neutral-400 transition-[color,opacity,transform] duration-200 hover:text-neutral-700 hover:opacity-100">
      <span className="flex h-5 w-5 items-center justify-center text-current" aria-hidden="true">
        {tech.icon}
      </span>
      <span className="whitespace-nowrap text-[0.72rem] font-semibold tracking-[0.08em] text-current uppercase">
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
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-10"
        style={{
          background:
            "linear-gradient(to bottom, rgba(255,255,255,0.74) 0%, rgba(255,255,255,0) 100%)",
        }}
        aria-hidden
      />

      <div className="relative mx-auto max-w-[1440px] px-4 sm:px-6 lg:px-20">
        <p className="mb-5 text-center text-[0.66rem] font-semibold uppercase tracking-[0.28em] text-neutral-400">
          {label}
        </p>

        <div className="flex flex-wrap items-center justify-center gap-x-7 gap-y-2 sm:gap-x-9">
          {STACK.map((tech) => <TechItem key={tech.name} tech={tech} />)}
        </div>
      </div>
    </section>
  );
}
