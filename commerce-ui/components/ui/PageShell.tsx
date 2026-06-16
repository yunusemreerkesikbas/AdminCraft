type PageShellProps = {
  eyebrow?: string;
  title: string;
  description: string;
  children?: React.ReactNode;
};

export function PageShell({
  eyebrow,
  title,
  description,
  children,
}: PageShellProps) {
  return (
    <main className="mx-auto flex min-h-[calc(100vh-96px)] max-w-6xl flex-col justify-center px-6 py-16">
      <section className="max-w-3xl">
        {eyebrow ? (
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[var(--muted)]">
            {eyebrow}
          </p>
        ) : null}
        <h1 className="mt-4 text-4xl font-semibold tracking-normal md:text-6xl">
          {title}
        </h1>
        <p className="mt-5 max-w-2xl text-base leading-7 text-[var(--muted)]">
          {description}
        </p>
        {children ? <div className="mt-8">{children}</div> : null}
      </section>
    </main>
  );
}
