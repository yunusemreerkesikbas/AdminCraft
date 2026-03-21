import Link from "next/link";
import type { ReactNode } from "react";
import styles from "../theme.module.css";
import { LeafIcon, UpArrowIcon } from "./icons";

export function ThemeTag({
  children,
  className = "",
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <span className={`${styles.sectionTag} ${className}`.trim()}>
      <span className={styles.sectionTagIcon}>
        <LeafIcon />
      </span>
      {children}
    </span>
  );
}

export function ThemeButton({
  href,
  children,
  dark = false,
  external = false,
}: {
  href: string;
  children: ReactNode;
  dark?: boolean;
  external?: boolean;
}) {
  const className = `${styles.whiteButton} ${dark ? styles.whiteButtonDark : ""}`;
  const content = (
    <>
      {children}
      <span className={styles.whiteButtonIcon}>
        <LeafIcon />
      </span>
    </>
  );

  if (external) {
    return (
      <a href={href} className={className} target="_blank" rel="noopener noreferrer">
        {content}
      </a>
    );
  }

  return (
    <Link href={href} className={className}>
      {content}
    </Link>
  );
}

export function ThemeCircleButton({
  href,
  children,
  external = false,
}: {
  href: string;
  children: ReactNode;
  external?: boolean;
}) {
  const content = (
    <>
      <span className={styles.circleButtonText}>{children}</span>
      <span className={styles.circleButtonIcon}>
        <UpArrowIcon />
      </span>
      <i className={styles.circleButtonDot} />
    </>
  );

  if (external) {
    return (
      <a href={href} className={styles.circleButton} target="_blank" rel="noopener noreferrer">
        {content}
      </a>
    );
  }

  return (
    <Link href={href} className={styles.circleButton}>
      {content}
    </Link>
  );
}
