"use client";
import Image from "next/image";
import { useTranslations } from "next-intl";
import { ArrowUpRightIcon } from "@/components/icons";
import { Reveal } from "@/components/Reveal";

type Project = {
  name: string;
  url: string;
  image: string;
  tags: string[];
  description: string;
};

const PROJECTS: Project[] = [
  {
    name: "Quick Alarm",
    url: "https://quickalarm.com.tr",
    image: "/images/portfolio/quickalarm.webp",
    tags: ["Kurumsal Site", "Landing Page"],
    description: "İzmir merkezli alarm, kamera ve yangın sistemleri firması için uçtan uca kurumsal web sitesi.",
  },
  {
    name: "Ajans Tanıtım",
    url: "https://s1-demo.craftive.io/en",
    image: "/images/portfolio/agency.webp",
    tags: ["Ajans", "Tanıtım Sitesi"],
    description: "Ajanslar ve hizmet firmaları için hazır altyapı üzerinde çalışan canlı tanıtım projesi.",
  },
  {
    name: "Yemre Dev",
    url: "https://yemredev.com/tr",
    image: "/images/portfolio/yemredev.webp",
    tags: ["AI Asistan", "Portfolyo"],
    description: "Yapay zeka destekli kişisel asistan ve yazılım geliştirici portföy sitesi.",
  },
];

export function Social() {
  const t = useTranslations("social");

  return (
    <section id="social" className="bg-surface py-20 overflow-hidden">
      <div className="max-w-6xl mx-auto px-6">
        <Reveal>
          <div className="flex flex-col md:flex-row md:items-end justify-between mb-12 gap-6">
            <div>
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white border border-line mb-5">
                <span className="size-1.5 rounded-full bg-[#0E121B]" />
                <span className="text-[13px] font-medium text-ink-muted">{t("badge")}</span>
              </div>
              <h2 className="font-heading text-[32px] md:text-[48px] leading-[1.1] font-medium tracking-[-0.02em] text-ink max-w-xl">
                {t("headline")}
              </h2>
              <p className="mt-4 text-[15px] text-ink-muted max-w-md leading-relaxed">
                {t("subhead")}
              </p>
            </div>
          </div>
        </Reveal>
      </div>

      <div className="overflow-hidden mask-fade-x">
        <div className="flex gap-5 marquee-x px-6" style={{ width: "max-content" }}>
          {[...PROJECTS, ...PROJECTS].map((project, i) => (
            <a
              key={i}
              href={project.url}
              target="_blank"
              rel="noopener noreferrer"
              className="group relative w-[300px] h-[420px] md:w-[450px] md:h-[620px] flex-shrink-0 rounded-3xl overflow-hidden cursor-pointer block"
              aria-label={`${project.name} — canlı siteyi ziyaret et`}
            >
              <Image
                src={project.image}
                alt={project.name}
                fill
                className="object-cover object-top transition-transform duration-700 group-hover:scale-105"
                sizes="(max-width: 768px) 300px, 450px"
              />

              <div className="absolute inset-0 bg-gradient-to-b from-black/25 via-black/10 to-black/80 transition-opacity duration-300 group-hover:to-black/90" />

              {/* Top: tags + external link button */}
              <div className="absolute top-5 left-5 right-5 flex items-start justify-between gap-2">
                <div className="flex flex-wrap gap-1.5">
                  {project.tags.map((tag) => (
                    <span
                      key={tag}
                      className="px-3 py-1.5 rounded-full bg-white/15 backdrop-blur-sm border border-white/20 text-white text-[11px] font-semibold tracking-[0.04em] uppercase"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
                <span className="size-9 rounded-full bg-white/15 backdrop-blur-sm border border-white/20 flex items-center justify-center text-white flex-shrink-0 transition-all duration-200 group-hover:bg-white group-hover:text-ink">
                  <ArrowUpRightIcon size={15} />
                </span>
              </div>

              {/* Bottom: project name + description */}
              <div className="absolute bottom-5 left-5 right-5">
                <p className="text-white/70 text-[12px] leading-snug mb-1.5">
                  {project.description}
                </p>
                <div className="font-heading text-[22px] font-medium text-white leading-tight">
                  {project.name}
                </div>
              </div>
            </a>
          ))}
        </div>
      </div>
    </section>
  );
}
