import { AnimateInView } from "./AnimateInView";

type TrustedByProps = {
  title: string;
};

export function TrustedBy({ title }: TrustedByProps) {
  return (
    <section className="bg-[var(--color-light-neutral-1)] py-12">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col items-center gap-10 px-4 sm:px-10 md:gap-10 lg:px-20">
        <AnimateInView>
          <p className="text-center text-xl font-medium leading-[1.1] text-[var(--color-dark-neutral-2)] md:text-2xl md:leading-[1.1]">
            {title}
          </p>
        </AnimateInView>
        <div className="flex w-full flex-wrap items-center justify-center gap-8 md:gap-12">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <AnimateInView key={i} delay={(i % 3) as 0 | 1 | 2} variant="scaleIn">
              <div
                className="h-8 w-24 rounded-lg border border-[var(--color-shade)] bg-[var(--color-light-neutral-2)]/80 transition-transform duration-300 hover:scale-105 md:w-32"
                aria-hidden
              />
            </AnimateInView>
          ))}
        </div>
      </div>
    </section>
  );
}
