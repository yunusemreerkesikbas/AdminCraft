import type { CmsComponentProps } from "@/components/cms/registry";

export default function ParagraphRenderer({ component }: CmsComponentProps) {
  if (!component.description && !component.title) {
    return null;
  }

  return (
    <div className="prose prose-slate max-w-none p-6">
      {component.title ? <h2>{component.title}</h2> : null}
      {component.description ? <p>{component.description}</p> : null}
    </div>
  );
}
