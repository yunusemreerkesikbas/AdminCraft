import Image from "next/image";
import { ResponsiveMediaDelivery } from "@/lib/types";
import { buildMediaUrl } from "@/lib/utils";

export default function CmsImage({
  media,
  alt = "",
  className,
}: {
  media?: ResponsiveMediaDelivery | null;
  alt?: string;
  className?: string;
}) {
  if (!media?.desktop && !media?.mobile) {
    return null;
  }

  const desktop = media.desktop ?? undefined;
  const mobile = media.mobile ?? undefined;
  const fallbackUrl = desktop?.url ?? mobile?.url ?? "";

  if (!fallbackUrl) {
    return null;
  }

  const width = desktop?.width ?? mobile?.width ?? 1200;
  const height = desktop?.height ?? mobile?.height ?? 800;

  return (
    <Image
      className={className}
      src={buildMediaUrl(fallbackUrl)}
      alt={alt}
      width={width}
      height={height}
      sizes={
        mobile?.url
          ? `(max-width: 768px) 100vw, ${width}px`
          : `${width}px`
      }
    />
  );
}
