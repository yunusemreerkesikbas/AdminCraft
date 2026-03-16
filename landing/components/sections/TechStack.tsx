"use client";

/* ── SVG logos ───────────────────────────────────────────────────── */

const SpringIcon = () => (
  <img
    src="/spring-boot.svg"
    alt="Spring Boot"
    className="h-7 w-7 shrink-0 brightness-0 invert"
    loading="lazy"
  />
);

const JavaIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 256 256" aria-hidden="true">
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
    alt="MySQL"
    className="h-7 w-7 shrink-0 brightness-0 invert"
    loading="lazy"
  />
);

const AngularIcon = () => (
  <img
    src="/angular.svg"
    alt="Angular"
    className="h-7 w-7 shrink-0 brightness-0 invert"
    loading="lazy"
  />
);

const TypeScriptIcon = () => (
  <img
    src="/typescript.svg"
    alt="TypeScript"
    className="h-7 w-7 shrink-0"
    loading="lazy"
  />
);

const NextjsIcon = () => (
  <img
    src="/nextjs.svg"
    alt="Next.js"
    className="h-7 w-7 shrink-0 brightness-0 invert"
    loading="lazy"
  />
);

const DockerIcon = () => (
  <img
    src="/docker.svg"
    alt="Docker"
    className="h-7 w-7 shrink-0"
    loading="lazy"
  />
);

const CloudflareIcon = () => (
  <img
    src="/cloudflare.svg"
    alt="Cloudflare"
    className="h-7 w-7 shrink-0"
    loading="lazy"
  />
);

const DigitalOceanIcon = () => (
  <img
    src="/digitalocean.svg"
    alt="DigitalOcean"
    className="h-7 w-7 shrink-0"
    loading="lazy"
  />
);

const GitHubIcon = () => (
  <img
    src="/github.svg"
    alt="GitHub"
    className="h-7 w-7 shrink-0 brightness-0 invert"
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
    <div
      className="group flex cursor-default flex-col items-center gap-2 px-4 opacity-70 transition-all duration-300 hover:opacity-100"
    >
      <div className="flex h-11 w-11 items-center justify-center rounded-full bg-white/5 ring-1 ring-white/5 shadow-sm shadow-black/40 group-hover:bg-white/10 group-hover:ring-white/20">
        <span className="text-white" role="img" aria-label={tech.name}>
          {tech.icon}
        </span>
      </div>
      <span className="text-[11px] font-semibold tracking-wide text-white/70 whitespace-nowrap">
        {tech.name}
      </span>
    </div>
  );
}

type TechStackProps = { label: string };

export function TechStack({ label }: TechStackProps) {
  return (
    <section
      className="relative overflow-hidden py-14"
      style={{
        background: "linear-gradient(160deg, #040d1c 0%, #060f22 60%, #0a1530 100%)",
      }}
    >
      {/* Dot pattern */}
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.025]"
        style={{
          backgroundImage: "radial-gradient(circle, #fff 1px, transparent 1px)",
          backgroundSize: "24px 24px",
        }}
        aria-hidden
      />
      <div className="pointer-events-none absolute inset-x-0 top-0 h-px"
        style={{ background: "linear-gradient(90deg, transparent, rgba(255,255,255,0.07) 50%, transparent)" }}
        aria-hidden />
      <div className="pointer-events-none absolute inset-x-0 bottom-0 h-px"
        style={{ background: "linear-gradient(90deg, transparent, rgba(255,255,255,0.07) 50%, transparent)" }}
        aria-hidden />

      <div className="relative mx-auto max-w-[1440px] px-4 sm:px-6 lg:px-20">
        <p className="mb-10 text-center text-[10px] font-semibold uppercase tracking-[0.22em] text-white/30">
          {label}
        </p>

        <div className="flex flex-wrap items-center justify-center gap-2">
          {STACK.map(tech => <TechItem key={tech.name} tech={tech} />)}
        </div>
      </div>
    </section>
  );
}
