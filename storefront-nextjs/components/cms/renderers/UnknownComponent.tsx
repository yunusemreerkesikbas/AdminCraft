import type { CmsComponentProps } from "@/components/cms/registry";

export default function UnknownComponent({ component }: CmsComponentProps) {
  if (process.env.NODE_ENV !== "development") {
    return null;
  }

  return (
    <div
      style={{
        border: "2px dashed #ef4444",
        borderRadius: "6px",
        padding: "12px 16px",
        color: "#ef4444",
        fontSize: "13px",
        fontFamily: "monospace",
      }}
    >
      Unknown component type: <strong>{component.type}</strong> (uid: {component.uid})
    </div>
  );
}
