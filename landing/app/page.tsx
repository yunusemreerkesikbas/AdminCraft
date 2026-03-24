import Link from "next/link";

export default function RootPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-[var(--color-light-neutral-1)] p-6 text-center">
      <div className="space-y-3">
        <h1 className="font-heading text-2xl font-bold text-[var(--color-dark-neutral-1)]">Craftive</h1>
        <p className="text-sm text-[var(--color-dark-neutral-2)]">Select your language to continue.</p>
        <div className="flex items-center justify-center gap-4">
          <Link className="font-semibold text-[var(--color-theme-1)]" href="/en">
            English
          </Link>
          <Link className="font-semibold text-[var(--color-theme-1)]" href="/tr">
            Turkce
          </Link>
        </div>
      </div>
    </main>
  );
}
