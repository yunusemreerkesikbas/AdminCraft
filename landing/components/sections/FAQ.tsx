import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import { AnimateInView } from "@/components/AnimateInView";

type FAQItem = { question: string; answer: string };
type FAQGroup = { title: string; items: FAQItem[] };

type FAQContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  groups: FAQGroup[];
};

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
            <AnimateInView className="animate-in-view-delay-1">
              <h2 className="font-heading text-3xl font-semibold tracking-tight text-[var(--color-dark-neutral-1)] sm:text-4xl">
                {content.heading}
              </h2>
            </AnimateInView>
            <AnimateInView className="animate-in-view-delay-2">
              <p className="text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
            </AnimateInView>
          </div>

          <AnimateInView className="animate-in-view-delay-2">
            <Accordion type="single" collapsible className="w-full space-y-3">
              {items.map((item, index) => (
                <AccordionItem
                  key={item.question}
                  value={`faq-${index}`}
                  className="rounded-xl border border-[var(--color-shade)] bg-white px-5 shadow-sm"
                >
                  <AccordionTrigger className="py-4 text-left font-heading text-sm font-semibold text-[var(--color-dark-neutral-1)] hover:no-underline">
                    {item.question}
                  </AccordionTrigger>
                  <AccordionContent className="pb-4 text-sm leading-relaxed text-[var(--color-dark-neutral-2)]">
                    {item.answer}
                  </AccordionContent>
                </AccordionItem>
              ))}
            </Accordion>
          </AnimateInView>
        </div>
      </div>
    </section>
  );
}
