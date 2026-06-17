type PageShellProps = {
  eyebrow?: string;
  title: string;
  description: string;
  actions?: React.ReactNode;
  visual?: React.ReactNode;
  children?: React.ReactNode;
};

export function PageShell({
  eyebrow,
  title,
  description,
  actions,
  visual,
  children,
}: PageShellProps) {
  return (
    <main id="main-content" className="page-shell commerce-container">
      <div className="page-shell__grid">
        <section className="page-shell__intro">
          {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
          <h1 className="page-title">{title}</h1>
          <p className="page-description">{description}</p>
          {actions ? <div className="page-actions">{actions}</div> : null}
        </section>
        {visual}
      </div>
      {children ? <div className="page-body">{children}</div> : null}
    </main>
  );
}
