"use client";

/* ── SVG logos ───────────────────────────────────────────────────── */

const SpringIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 100 100" fill="currentColor">
    {/* Asymmetric leaf — Spring logo shape */}
    <path d="M78 8 C55 2 20 18 10 48 C4 65 12 84 30 90 C50 97 72 88 82 68 C92 50 90 25 78 8 Z" />
    {/* Small dot — Spring Boot badge */}
    <circle cx="76" cy="10" r="6" />
  </svg>
);

const JavaIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 80 100" fill="currentColor">
    {/* steam */}
    <path d="M22 26 C18 16 26 9 22 2" stroke="currentColor" strokeWidth="4.5" strokeLinecap="round" fill="none" />
    <path d="M40 26 C36 16 44 9 40 2" stroke="currentColor" strokeWidth="4.5" strokeLinecap="round" fill="none" />
    <path d="M58 26 C54 16 62 9 58 2" stroke="currentColor" strokeWidth="4.5" strokeLinecap="round" fill="none" />
    {/* cup body */}
    <path d="M8 34 L8 37 Q8 40 11 40 L69 40 Q72 40 72 37 L72 34 Z" />
    <path d="M11 40 L17 90 Q17 94 21 94 L59 94 Q63 94 63 90 L69 40 Z" />
    {/* handle */}
    <path d="M69 52 Q92 52 92 67 Q92 82 69 82" stroke="currentColor" strokeWidth="7" fill="none" strokeLinecap="round" />
  </svg>
);

const MySQLIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 100 100" fill="currentColor">
    {/* Database cylinder */}
    <ellipse cx="50" cy="20" rx="38" ry="12" />
    <path d="M12 20 L12 80 Q12 92 50 92 Q88 92 88 80 L88 20 Q88 32 50 32 Q12 32 12 20 Z" />
    <ellipse cx="50" cy="50" rx="38" ry="12" fill="none" stroke="currentColor" strokeWidth="4"
      style={{ fill: "none" }}
    />
  </svg>
);

const AngularIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 250 250" fill="currentColor">
    <path d="M125 30L31.9 63.2l14.2 123.1L125 230l78.9-43.7 14.2-123.1z" />
    <path d="M125 52.1L66.8 182.6h21.7l11.7-29.2h49.4l11.7 29.2H183L125 52.1zm17 83.3h-34l17-40.9 17 40.9z" fill="black" />
  </svg>
);

const TypeScriptIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 100 100" fill="currentColor">
    <rect width="100" height="100" rx="8" />
    <path d="M35.5 62.2v5c1.7 1 3.7 1.7 5.9 2.1 2.2.4 4.5.6 6.9.6 2.3 0 4.5-.2 6.5-.7s3.8-1.2 5.3-2.2c1.5-1 2.7-2.3 3.6-3.9.9-1.6 1.3-3.4 1.3-5.6 0-1.6-.2-3-.7-4.2-.5-1.2-1.2-2.3-2.2-3.3-1-.9-2.2-1.8-3.6-2.5-1.4-.8-3-1.5-4.9-2.2-1.3-.5-2.5-.9-3.5-1.4-1-.5-1.9-1-2.6-1.5-.7-.5-1.2-1-1.6-1.6-.4-.6-.6-1.2-.6-1.9 0-.7.2-1.3.5-1.8.3-.5.8-1 1.4-1.4.6-.4 1.3-.7 2.1-.9.8-.2 1.7-.3 2.7-.3.7 0 1.4.1 2.2.2.8.1 1.5.3 2.3.6.8.3 1.5.6 2.2 1 .7.4 1.3.9 1.8 1.4v-5c-1.3-.6-2.7-1-4.2-1.3-1.5-.3-3.1-.4-4.9-.4-2.2 0-4.3.2-6.2.7-1.9.5-3.6 1.2-5.1 2.2-1.4 1-2.6 2.3-3.4 3.8-.8 1.5-1.2 3.3-1.2 5.3 0 2.6.7 4.8 2.2 6.7 1.5 1.9 3.8 3.3 6.9 4.4 1.4.5 2.7 1 3.8 1.5 1.2.5 2.2 1 3 1.6.8.6 1.5 1.2 1.9 1.9.5.7.7 1.5.7 2.4 0 .7-.1 1.3-.4 1.9-.3.6-.7 1.1-1.3 1.5-.6.4-1.3.8-2.2 1-.9.3-1.9.4-3.1.4-2 0-3.9-.4-5.8-1.2-2-.7-3.8-1.9-5.3-3.4z" fill="black" />
    <path d="M72.5 38.5H53v5h8v24h5.5V43.5H72.5v-5z" fill="black" />
  </svg>
);

const NextjsIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 180 180" fill="currentColor">
    <mask id="nj-mask" maskUnits="userSpaceOnUse" x="0" y="0" width="180" height="180">
      <circle cx="90" cy="90" r="90" fill="white" />
    </mask>
    <circle cx="90" cy="90" r="90" />
    <g mask="url(#nj-mask)">
      <path d="M149.508 157.52L69.142 54H54v71.97h12.374V71.408l72.435 94.481a90.54 90.54 0 0 0 10.699-8.37z" fill="black" />
      <rect x="115.625" y="54" width="12.461" height="72" fill="black" />
    </g>
  </svg>
);

const DockerIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 100 100" fill="currentColor">
    {/* Container stack — 3 rows of boxes */}
    <rect x="8"  y="58" width="20" height="16" rx="2" />
    <rect x="32" y="58" width="20" height="16" rx="2" />
    <rect x="56" y="58" width="20" height="16" rx="2" />
    <rect x="8"  y="38" width="20" height="16" rx="2" />
    <rect x="32" y="38" width="20" height="16" rx="2" />
    <rect x="56" y="38" width="20" height="16" rx="2" />
    <rect x="32" y="18" width="20" height="16" rx="2" />
    {/* Whale tail arc */}
    <path d="M78 58 Q95 50 90 38 Q86 30 78 32" stroke="currentColor" strokeWidth="4" fill="none" strokeLinecap="round" />
    <path d="M4 74 Q4 88 20 88 L76 88 Q88 88 88 76" stroke="currentColor" strokeWidth="4" fill="none" strokeLinecap="round" />
  </svg>
);

const CloudflareIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 109 82" fill="currentColor">
    <path d="M73 46.2c.5-1.8.8-3.7.8-5.7 0-11.9-9.7-21.6-21.6-21.6-7.1 0-13.5 3.5-17.5 8.8-2.4-1.8-5.4-2.9-8.6-2.9-7.8 0-14.1 6.3-14.1 14.1 0 .5 0 1.1.1 1.6C5.7 41.9 2 47 2 53c0 8.3 6.7 15 15 15H79c7.5 0 13.5-6 13.5-13.5 0-6.4-4.5-11.8-10.6-13.1L73 46.2z" />
  </svg>
);

const DigitalOceanIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 100 115" fill="currentColor">
    <path d="M50 0C22.4 0 0 22.4 0 50s22.4 50 50 50 50-22.4 50-50S77.6 0 50 0zm0 78C33.4 78 20 64.6 20 48S33.4 18 50 18s30 13.4 30 30-13.4 30-30 30z" />
    <rect x="42" y="100" width="16" height="15" rx="2" />
    <rect x="20" y="108" width="22" height="8" rx="2" />
  </svg>
);

const GitHubIcon = () => (
  <svg className="h-7 w-7 shrink-0" viewBox="0 0 98 96" fill="currentColor">
    <path d="M48.854 0C21.839 0 0 22 0 49.217c0 21.756 13.993 40.172 33.405 46.69 2.427.49 3.316-1.059 3.316-2.362 0-1.141-.08-5.052-.08-9.127-13.59 2.934-16.42-5.867-16.42-5.867-2.184-5.704-5.42-7.17-5.42-7.17-4.448-3.015.324-3.015.324-3.015 4.934.326 7.523 5.052 7.523 5.052 4.367 7.496 11.404 5.378 14.235 4.074.404-3.178 1.699-5.378 3.074-6.6-10.839-1.141-22.243-5.378-22.243-24.283 0-5.378 1.94-9.778 5.014-13.2-.485-1.222-2.184-6.275.486-13.038 0 0 4.125-1.304 13.426 5.052a46.97 46.97 0 0 1 12.214-1.63c4.125 0 8.33.571 12.213 1.63 9.302-6.356 13.427-5.052 13.427-5.052 2.67 6.763.97 11.816.485 13.038 3.155 3.422 5.015 7.822 5.015 13.2 0 18.905-11.404 23.06-22.324 24.283 1.78 1.548 3.316 4.481 3.316 9.126 0 6.6-.08 11.897-.08 13.526 0 1.304.89 2.853 3.316 2.364 19.412-6.52 33.405-24.935 33.405-46.691C97.707 22 75.788 0 48.854 0z" />
  </svg>
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
      className="group flex cursor-default flex-col items-center gap-3 px-6 opacity-40 transition-opacity duration-300 hover:opacity-100"
    >
      <span className="text-white" role="img" aria-label={tech.name}>{tech.icon}</span>
      <span className="text-[11px] font-semibold tracking-wide text-white whitespace-nowrap">
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
