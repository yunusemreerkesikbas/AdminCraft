import Image from "next/image";
import { AnimateInView } from "./AnimateInView";

type TestimonialItem = {
  name: string;
  role: string;
  quote: string;
  avatarUrl?: string;
};

type TestimonialsProps = {
  sectionTag: string;
  heading: string;
  subheading: string;
  items: TestimonialItem[];
};

const DEFAULT_AVATARS = [
  "https://framerusercontent.com/images/rG0szMGyPKbXIZOrw5rO7PyoitY.png",
  "https://framerusercontent.com/images/xkA15EjvUVWeYMWSTKqleApRmNk.png",
];

export function Testimonials({ sectionTag, heading, subheading, items }: TestimonialsProps) {
  return (
    <section id="testimonials" className="bg-[var(--color-light-neutral-1)] py-20">
      <div className="mx-auto flex max-w-[1440px] flex-col items-center gap-20 px-4 sm:px-10 lg:px-20">
        <AnimateInView>
          <div className="flex max-w-[950px] flex-col items-center gap-4 text-center">
            <p className="text-sm font-medium uppercase tracking-wide text-[var(--color-dark-neutral-2)]">
              {sectionTag}
            </p>
            <h2 className="font-heading text-3xl font-semibold leading-tight text-[var(--color-dark-neutral-1)] md:text-4xl">
              {heading}
            </h2>
            <p className="text-base leading-relaxed text-[var(--color-dark-neutral-2)]">
              {subheading}
            </p>
          </div>
        </AnimateInView>
        <div className="grid w-full gap-6 md:grid-cols-2 lg:grid-cols-3">
          {items.map((item, i) => (
            <AnimateInView key={i} delay={i % 3 as 0 | 1 | 2} variant="scaleIn">
            <div
              className="card-hover group flex flex-col gap-4 rounded-xl border border-[var(--color-shade)] bg-white p-6"
            >
              <p className="text-base leading-relaxed text-[var(--color-dark-neutral-2)]">
                &ldquo;{item.quote}&rdquo;
              </p>
              <div className="flex items-center gap-3">
                <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full bg-[var(--color-shade)] ring-2 ring-white transition-transform duration-300 group-hover:scale-105">
                  <Image
                    src={item.avatarUrl ?? DEFAULT_AVATARS[i] ?? ""}
                    alt=""
                    width={48}
                    height={48}
                    className="object-cover"
                    unoptimized
                  />
                </div>
                <div>
                  <p className="font-medium text-[var(--color-dark-neutral-1)]">
                    {item.name}
                  </p>
                  <p className="text-sm text-[var(--color-dark-neutral-2)]">
                    {item.role}
                  </p>
                </div>
              </div>
            </div>
            </AnimateInView>
          ))}
        </div>
      </div>
    </section>
  );
}
