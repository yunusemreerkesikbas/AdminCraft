"use client";

import styles from "./shell.module.css";

function ArrowIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
      <path d="M3 9H15" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <path d="M10 4L15 9L10 14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

export default function NewsletterForm({
  placeholder,
  buttonLabel,
}: {
  placeholder: string;
  buttonLabel: string;
}) {
  return (
    <form onSubmit={(e) => e.preventDefault()}>
      <div className={styles.newsletterField}>
        <input
          type="email"
          placeholder={placeholder}
          aria-label={placeholder}
          className={styles.newsletterInput}
        />
        <button
          type="submit"
          className={styles.newsletterButton}
          aria-label={buttonLabel}
        >
          <ArrowIcon />
        </button>
      </div>
    </form>
  );
}
