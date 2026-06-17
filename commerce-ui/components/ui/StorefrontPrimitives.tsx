import Link from "next/link";

type ActionLinkProps = {
  href: string;
  label: string;
  variant?: "primary" | "secondary";
};

export function ActionLink({
  href,
  label,
  variant = "primary",
}: ActionLinkProps) {
  return (
    <Link
      href={href}
      className={
        variant === "primary"
          ? "commerce-action"
          : "commerce-action commerce-action--secondary"
      }
    >
      {label}
    </Link>
  );
}

export function DisabledAction({ label }: { label: string }) {
  return (
    <button type="button" className="commerce-action-disabled" disabled>
      {label}
    </button>
  );
}

type ProductFrameProps = {
  label: string;
  status: string[];
};

export function ProductFrame({ label, status }: ProductFrameProps) {
  return (
    <aside className="visual-frame product-frame" aria-label={label}>
      <div className="product-frame__media" aria-hidden="true" />
      <div className="product-frame__meta">
        <div className="product-frame__line" aria-hidden="true" />
        <div
          className="product-frame__line product-frame__line--short"
          aria-hidden="true"
        />
        <div className="product-frame__status">
          {status.map((item) => (
            <span key={item} className="quiet-chip">
              {item}
            </span>
          ))}
        </div>
      </div>
    </aside>
  );
}

export type FrameRow = {
  label: string;
  value: string;
};

type ReceiptFrameProps = {
  title: string;
  note?: string;
  rows: FrameRow[];
  totalLabel?: string;
  totalValue?: string;
};

export function ReceiptFrame({
  title,
  note,
  rows,
  totalLabel,
  totalValue,
}: ReceiptFrameProps) {
  return (
    <aside className="surface-panel receipt-frame" aria-label={title}>
      <h2 className="frame-title">{title}</h2>
      {note ? <p className="frame-note">{note}</p> : null}
      <div className="mt-5">
        {rows.map((row) => (
          <div key={row.label} className="receipt-row">
            <span className="row-description">{row.label}</span>
            <strong className="row-title">{row.value}</strong>
          </div>
        ))}
      </div>
      {totalLabel && totalValue ? (
        <div className="receipt-row receipt-total">
          <span>{totalLabel}</span>
          <span>{totalValue}</span>
        </div>
      ) : null}
    </aside>
  );
}

export type StepItem = {
  title: string;
  description: string;
};

export function StepFrame({
  title,
  steps,
}: {
  title: string;
  steps: StepItem[];
}) {
  return (
    <aside className="surface-panel step-frame" aria-label={title}>
      <h2 className="frame-title">{title}</h2>
      <div className="mt-5">
        {steps.map((step, index) => (
          <div key={step.title} className="step-row">
            <span className="step-index" aria-hidden="true">
              {index + 1}
            </span>
            <span className="min-w-0">
              <strong className="row-title block">{step.title}</strong>
              <span className="row-description block">{step.description}</span>
            </span>
          </div>
        ))}
      </div>
    </aside>
  );
}

export type ListItem = {
  title: string;
  description: string;
};

export function CommerceList({
  title,
  items,
}: {
  title: string;
  items: ListItem[];
}) {
  return (
    <section className="surface-panel commerce-list" aria-label={title}>
      <h2 className="frame-title">{title}</h2>
      <div className="mt-5">
        {items.map((item) => (
          <article key={item.title} className="commerce-list__row">
            <h3 className="row-title">{item.title}</h3>
            <p className="row-description">{item.description}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export function CapabilityGrid({
  title,
  items,
}: {
  title: string;
  items: ListItem[];
}) {
  return (
    <section className="capability-grid" aria-label={title}>
      {items.map((item) => (
        <article key={item.title} className="capability-item">
          <h2 className="capability-title">{item.title}</h2>
          <p className="capability-description">{item.description}</p>
        </article>
      ))}
    </section>
  );
}
