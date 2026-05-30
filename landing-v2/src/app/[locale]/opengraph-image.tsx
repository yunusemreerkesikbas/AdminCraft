import { ImageResponse } from "next/og";
import { routing } from "@/i18n/routing";

export const alt = "Craftive — one platform, every configuration";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

const TAGLINE: Record<string, string> = {
  tr: "Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim.",
  en: "Tell us your idea, we build your digital product.",
};

// Brand-colored social card. Text-only (no external assets) so it stays well
// under the 500KB bundle limit and renders with next/og's default font.
export default async function Image({ params }: { params: Promise<{ locale: string }> }) {
  const { locale } = await params;
  const tagline = TAGLINE[locale] ?? TAGLINE[routing.defaultLocale];

  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
          background: "#0E121B",
          padding: "80px",
          color: "#ffffff",
          fontFamily: "sans-serif",
        }}
      >
        <div style={{ display: "flex", fontSize: 40, fontWeight: 700, letterSpacing: "-0.02em" }}>
          Craftive
        </div>
        <div
          style={{
            display: "flex",
            fontSize: 64,
            fontWeight: 700,
            lineHeight: 1.1,
            letterSpacing: "-0.03em",
            maxWidth: "900px",
          }}
        >
          {tagline}
        </div>
        <div style={{ display: "flex", fontSize: 28, color: "#9aa0ad", letterSpacing: "0.02em" }}>
          one platform, every configuration.
        </div>
      </div>
    ),
    { ...size },
  );
}
