"use client";

import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import { AnimateInView, StaggerContainer, StaggerItem } from "@/components/AnimateInView";

type FAQItem = { question: string; answer: string };
type FAQGroup = { title: string; items: FAQItem[] };
type FAQContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  groups: FAQGroup[];
};

function DecorativeSVG() {
  return (
    <div className="pointer-events-none mt-8 hidden opacity-40 lg:block" aria-hidden="true">
      <svg viewBox="0 0 200 200" className="w-48 text-neutral-300">
        {/* Abstract geometric pattern */}
        <circle cx="100" cy="100" r="80" fill="none" stroke="currentColor" strokeWidth="0.5" strokeDasharray="4 6" />
        <circle cx="100" cy="100" r="55" fill="none" stroke="currentColor" strokeWidth="0.5" strokeDasharray="3 5" />
        <circle cx="100" cy="100" r="30" fill="none" stroke="currentColor" strokeWidth="0.5" />

        {/* Question mark stylized */}
        <text x="100" y="115" textAnchor="middle" fontSize="48" fontWeight="700" fill="currentColor" opacity="0.15">
          ?
        </text>

        {/* Decorative dots */}
        <circle cx="40" cy="40" r="3" fill="currentColor" opacity="0.2" />
        <circle cx="160" cy="40" r="2" fill="currentColor" opacity="0.15" />
        <circle cx="40" cy="160" r="2" fill="currentColor" opacity="0.15" />
        <circle cx="160" cy="160" r="3" fill="currentColor" opacity="0.2" />
        <circle cx="100" cy="20" r="2" fill="currentColor" opacity="0.1" />
        <circle cx="100" cy="180" r="2" fill="currentColor" opacity="0.1" />
        <circle cx="20" cy="100" r="2" fill="currentColor" opacity="0.1" />
        <circle cx="180" cy="100" r="2" fill="currentColor" opacity="0.1" />

        {/* Connecting lines */}
        <line x1="40" y1="40" x2="65" y2="55" stroke="currentColor" strokeWidth="0.5" opacity="0.15" />
        <line x1="160" y1="40" x2="135" y2="55" stroke="currentColor" strokeWidth="0.5" opacity="0.15" />
        <line x1="40" y1="160" x2="65" y2="145" stroke="currentColor" strokeWidth="0.5" opacity="0.15" />
        <line x1="160" y1="160" x2="135" y2="145" stroke="currentColor" strokeWidth="0.5" opacity="0.15" />
      </svg>
    </div>
  );
}

export function FAQ({ content }: { content: FAQContent }) {
  const items = content.groups.flatMap((group) => group.items);

  return (
    <section id="faq" className="bg-[var(--color-light-neutral-1)] px-4 py-24 sm:px-6 lg:px-20">
      <div className="mx-auto max-w-[1440px]">
        <div className="grid gap-12 lg:grid-cols-[1fr_2fr] lg:gap-20">
          <div className="flex flex-col gap-4">
            <AnimateInView>
              <Badge variant="secondary" className="w-fit rounded-full border border-[var(--color-shade)] bg-white px-4 py-1 text-xs text-[var(--color-dark-neutral-2)]">
                {content.sectionTag}
              </Badge>
            </AnimateInView>
            <AnimateInView delay={1}>
              <h2 className="font-display text-3xl text-[var(--color-dark-neutral-1)] sm:text-4xl">
                {content.heading}
              </h2>
            </AnimateInView>
            <AnimateInView delay={2}>
              <p className="text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
            </AnimateInView>
            <AnimateInView delay={3} variant="fade">
              <DecorativeSVG />
            </AnimateInView>
          </div>

          <StaggerContainer staggerDelay={0.06}>
            <Accordion type="single" collapsible className="w-full space-y-3">
              {items.map((item, index) => (
                <StaggerItem key={item.question} variant="fadeUp">
                  <AccordionItem
                    value={`faq-${index}`}
                    className="rounded-xl border border-[var(--color-shade)] bg-white px-5 shadow-sm transition-shadow duration-300 data-[state=open]:shadow-md data-[state=open]:border-neutral-300"
                  >
                    <AccordionTrigger className="py-4 text-left font-heading text-sm font-semibold text-[var(--color-dark-neutral-1)] hover:no-underline">
                      {item.question}
                    </AccordionTrigger>
                    <AccordionContent className="pb-4 text-sm leading-relaxed text-[var(--color-dark-neutral-2)]">
                      {item.answer}
                    </AccordionContent>
                  </AccordionItem>
                </StaggerItem>
              ))}
            </Accordion>
          </StaggerContainer>
        </div>
      </div>
    </section>
  );
}
