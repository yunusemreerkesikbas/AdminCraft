import { ImageResponse } from "next/og";

export const alt = "Craftive — Multi-Tenant Project Platform";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default function Image() {
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
        fontFamily: "sans-serif",
      }}
    >
      <div
        style={{
          fontSize: 88,
          fontWeight: 700,
          color: "white",
          letterSpacing: "-2px",
        }}
      >
        Craftive
      </div>
      <div style={{ fontSize: 34, color: "#CBD5E1", marginTop: 12 }}>
        Multi-Tenant Project Platform
      </div>
      <div style={{ fontSize: 24, color: "#64748b", marginTop: 20 }}>
        craftive.io
      </div>
    </div>,
    { ...size },
  );
}
