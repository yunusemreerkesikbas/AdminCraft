"use client";

import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Users2, FileText, Layout, ShoppingCart, Building2, Database, Lock } from "lucide-react";
import { AnimateInView } from "@/components/AnimateInView";

type SegmentsContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
};

type Tenant = {
  key: string;
  name: string;
  type: string;
  domain: string;
  color: string;
  lightBg: string;
  Icon: React.ComponentType<{ className?: string }>;
  modules: string[];
  stats: { label: string; value: string }[];
};

const TENANTS: Tenant[] = [
  {
    key: "acme",
    name: "Acme Corp",
    type: "HR Portal",
    domain: "acme.craftive.io",
    color: "#8b8ef8",
    lightBg: "rgba(139,142,248,0.08)",
    Icon: Users2,
    modules: ["Employees", "Leave Mgmt", "Announcements", "Org Chart"],
    stats: [
      { label: "Employees", value: "48" },
      { label: "Departments", value: "6" },
      { label: "Pending", value: "3" },
    ],
  },
  {
    key: "techblog",
    name: "TechBlog",
    type: "Blog & CMS",
    domain: "techblog.craftive.io",
    color: "#f59e0b",
    lightBg: "rgba(245,158,11,0.08)",
    Icon: FileText,
    modules: ["Articles", "Categories", "Media", "SEO Tools"],
    stats: [
      { label: "Articles", value: "124" },
      { label: "Languages", value: "6" },
      { label: "Authors", value: "8" },
    ],
  },
  {
    key: "shopco",
    name: "ShopCo",
    type: "E-Commerce",
    domain: "shopco.craftive.io",
    color: "#10b981",
    lightBg: "rgba(16,185,129,0.08)",
    Icon: ShoppingCart,
    modules: ["Products", "Orders", "Customers", "Headless API"],
    stats: [
      { label: "Products", value: "248" },
      { label: "Orders", value: "1.2K" },
      { label: "Revenue", value: "₺84K" },
    ],
  },
  {
    key: "launchpad",
    name: "Launchpad",
    type: "Landing Page",
    domain: "launchpad.craftive.io",
    color: "#0ea5e9",
    lightBg: "rgba(14,165,233,0.08)",
    Icon: Layout,
    modules: ["Page Builder", "Forms", "A/B Testing", "Analytics"],
    stats: [
      { label: "Pages", value: "12" },
      { label: "A/B Tests", value: "3" },
      { label: "Leads", value: "340" },
    ],
  },
  {
    key: "medicore",
    name: "MediCore",
    type: "Corporate Site",
    domain: "medicore.craftive.io",
    color: "#f97316",
    lightBg: "rgba(249,115,22,0.08)",
    Icon: Building2,
    modules: ["Pages", "News", "Documents", "Contact"],
    stats: [
      { label: "Languages", value: "5" },
      { label: "Pages", value: "200+" },
      { label: "Documents", value: "48" },
    ],
  },
];

function TenantPanel({ tenant }: { tenant: Tenant }) {
  return (
    <div className="flex h-full flex-col gap-5 p-6 sm:p-8" style={{ background: tenant.lightBg }}>
      {/* Header */}
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-3">
          <div
            className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
            style={{ background: tenant.color + "22" }}
          >
            <tenant.Icon className="h-5 w-5" style={{ color: tenant.color }} />
          </div>
          <div>
            <p className="font-heading text-base font-bold text-[var(--color-dark-neutral-1)]">
              {tenant.name}
            </p>
            <p className="font-mono text-xs text-[var(--color-dark-neutral-2)]">{tenant.domain}</p>
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <span
            className="rounded-full px-2.5 py-1 text-[10px] font-bold text-white"
            style={{ background: tenant.color }}
          >
            {tenant.type}
          </span>
          <span className="flex items-center gap-1 rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-1 text-[10px] font-bold text-emerald-600">
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
            Live
          </span>
        </div>
      </div>

      {/* Modules */}
      <div>
        <p className="mb-2.5 text-[10px] font-bold uppercase tracking-widest text-[var(--color-dark-neutral-2)]/50">
          Active Modules
        </p>
        <div className="flex flex-wrap gap-2">
          {tenant.modules.map((mod) => (
            <span
              key={mod}
              className="rounded-lg border px-3 py-1.5 text-xs font-semibold"
              style={{
                borderColor: tenant.color + "40",
                color: tenant.color,
                background: tenant.color + "10",
              }}
            >
              {mod}
            </span>
          ))}
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-3">
        {tenant.stats.map((stat) => (
          <div
            key={stat.label}
            className="rounded-xl border bg-white p-3"
            style={{ borderColor: tenant.color + "30" }}
          >
            <p
              className="font-heading text-lg font-extrabold"
              style={{ color: tenant.color }}
            >
              {stat.value}
            </p>
            <p className="text-[10px] font-medium text-[var(--color-dark-neutral-2)]">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* DB isolation badge */}
      <div
        className="mt-auto flex items-center gap-2.5 rounded-xl border p-3"
        style={{ borderColor: tenant.color + "30", background: tenant.color + "08" }}
      >
        <div
          className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg"
          style={{ background: tenant.color + "20" }}
        >
          <Lock className="h-3.5 w-3.5" style={{ color: tenant.color }} />
        </div>
        <div>
          <p className="text-xs font-bold text-[var(--color-dark-neutral-1)]">
            Isolated Database
          </p>
          <p className="text-[10px] text-[var(--color-dark-neutral-2)]">
            {tenant.domain.split(".")[0]}_db · Data physically separated
          </p>
        </div>
        <Database className="ml-auto h-4 w-4 opacity-20" style={{ color: tenant.color }} />
      </div>
    </div>
  );
}

export function Segments({ content }: { content: SegmentsContent }) {
  const [active, setActive] = useState(TENANTS[0].key);
  const activeTenant = TENANTS.find((t) => t.key === active) ?? TENANTS[0];

  return (
    <section id="segments" className="bg-[var(--color-light-neutral-2)] px-4 py-24 sm:px-6 lg:px-20">
      <div className="mx-auto max-w-[1440px]">
        {/* Header */}
        <div className="flex flex-col items-center gap-4 text-center">
          <AnimateInView>
            <Badge
              variant="secondary"
              className="rounded-full border border-[var(--color-shade)] bg-white px-4 py-1 text-xs font-semibold text-[var(--color-dark-neutral-2)]"
            >
              {content.sectionTag}
            </Badge>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-1">
            <h2 className="font-heading max-w-xl text-3xl font-bold tracking-tight text-[var(--color-dark-neutral-1)] sm:text-4xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-2">
            <p className="max-w-xl text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
          </AnimateInView>
        </div>

        {/* Tenant switcher mockup */}
        <AnimateInView className="mt-14 animate-in-view-delay-2">
          <div className="overflow-hidden rounded-2xl border border-[var(--color-shade)] bg-white shadow-2xl shadow-black/8">
            {/* Browser chrome */}
            <div className="flex items-center gap-3 border-b border-[var(--color-shade)] bg-[var(--color-light-neutral-2)] px-4 py-3">
              <div className="flex gap-1.5">
                <div className="h-3 w-3 rounded-full bg-red-400" />
                <div className="h-3 w-3 rounded-full bg-yellow-400" />
                <div className="h-3 w-3 rounded-full bg-green-400" />
              </div>
              <div className="flex h-6 flex-1 items-center gap-2 rounded-lg border border-[var(--color-shade)] bg-white px-3 text-[11px] text-[var(--color-dark-neutral-2)]">
                <span className="opacity-40">🔒</span>
                <span className="opacity-50">admin.craftive.io</span>
              </div>
            </div>

            {/* App body */}
            <div className="flex min-h-[380px] divide-x divide-[var(--color-shade)]">
              {/* Left sidebar — tenant list */}
              <div className="flex w-52 shrink-0 flex-col bg-[var(--color-dark-neutral-1)]">
                {/* Brand */}
                <div className="border-b border-white/8 px-4 py-3.5">
                  <div className="flex items-center gap-2">
                    <div className="h-5 w-5 rounded-md bg-[var(--color-theme-3)]" />
                    <span className="font-heading text-sm font-bold text-white">AdminCraft</span>
                  </div>
                </div>

                {/* Tenants label */}
                <div className="px-4 pb-1 pt-4">
                  <p className="text-[9px] font-bold uppercase tracking-widest text-white/30">
                    Tenants ({TENANTS.length})
                  </p>
                </div>

                {/* Tenant rows */}
                <div className="flex flex-col gap-0.5 px-2 pb-3">
                  {TENANTS.map((tenant) => {
                    const isActive = tenant.key === active;
                    return (
                      <button
                        key={tenant.key}
                        onClick={() => setActive(tenant.key)}
                        aria-pressed={isActive}
                        className={`group flex w-full items-center gap-2.5 rounded-xl px-3 py-2.5 text-left transition-all duration-200 focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 ${
                          isActive ? "bg-white/10" : "hover:bg-white/5"
                        }`}
                      >
                        {/* Color dot */}
                        <div
                          className="h-2 w-2 shrink-0 rounded-full transition-all"
                          style={{
                            background: tenant.color,
                            opacity: isActive ? 1 : 0.4,
                            boxShadow: isActive ? `0 0 6px ${tenant.color}` : "none",
                          }}
                        />
                        <div className="min-w-0 flex-1">
                          <p
                            className={`truncate text-xs font-semibold transition-colors ${
                              isActive ? "text-white" : "text-white/50 group-hover:text-white/70"
                            }`}
                          >
                            {tenant.name}
                          </p>
                          <p
                            className="truncate text-[9px] font-medium transition-colors"
                            style={{ color: isActive ? tenant.color : "rgba(255,255,255,0.25)" }}
                          >
                            {tenant.type}
                          </p>
                        </div>
                        {isActive && (
                          <div
                            className="h-1.5 w-1.5 shrink-0 rounded-full"
                            style={{ background: tenant.color }}
                          />
                        )}
                      </button>
                    );
                  })}
                </div>

                {/* Bottom footer */}
                <div className="mt-auto border-t border-white/8 px-4 py-3">
                  <div className="flex items-center gap-2">
                    <div className="h-6 w-6 rounded-full bg-[var(--color-theme-3)]/80 flex items-center justify-center text-[9px] font-bold text-white">
                      A
                    </div>
                    <div>
                      <p className="text-[10px] font-semibold text-white/60">Admin</p>
                      <p className="text-[9px] text-white/30">Super Admin</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right panel — tenant detail */}
              <div className="flex-1 transition-all duration-300" key={active}>
                <TenantPanel tenant={activeTenant} />
              </div>
            </div>
          </div>

          {/* Below mockup callout */}
          <div className="mt-5 flex flex-wrap items-center justify-center gap-3">
            {TENANTS.map((t) => (
              <button
                key={t.key}
                onClick={() => setActive(t.key)}
                aria-pressed={t.key === active}
                className={`flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-semibold transition-all duration-200 focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 ${
                  t.key === active
                    ? "border-transparent text-white shadow-sm"
                    : "border-[var(--color-shade)] bg-white text-[var(--color-dark-neutral-2)] hover:border-[var(--color-dark-neutral-2)]/30"
                }`}
                style={
                  t.key === active
                    ? { background: t.color, boxShadow: `0 2px 12px ${t.color}50` }
                    : {}
                }
              >
                <t.Icon className="h-3 w-3" />
                {t.type}
              </button>
            ))}
          </div>
        </AnimateInView>
      </div>
    </section>
  );
}
