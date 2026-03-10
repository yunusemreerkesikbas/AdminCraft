import { ImageResponse } from "next/og";

export const runtime = "edge";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default async function Image({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  const isEN = locale === "en";

  const subtitle = isEN ? "Multi-Tenant Platform" : "Çok Kiracılı Platformu";

  return new ImageResponse(
    <div
      style={{
        background: "#0f172a",
        width: "100%",
        height: "100%",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        color: "white",
        fontFamily: "sans-serif",
      }}
    >
      <div style={{ fontSize: 64, fontWeight: "bold", letterSpacing: "-2px" }}>
        AdminCraft
      </div>
      <div style={{ fontSize: 28, marginTop: 16, opacity: 0.65 }}>
        {subtitle}
      </div>
      <div style={{ fontSize: 20, marginTop: 12, opacity: 0.4 }}>
        craftive.io
      </div>
    </div>,
    { ...size },
  );
}
