import type { NextConfig } from "next";
import path from "path";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./i18n/request.ts");

const outputMode = process.env.NEXT_OUTPUT as "export" | "standalone" | undefined;

const buildImageRemotePatterns = (): NextConfig["images"] => {
  const patterns: { protocol: "http" | "https"; hostname: string }[] = [];

  if (process.env.NODE_ENV !== "production") {
    patterns.push(
      { protocol: "http", hostname: "127.0.0.1" },
      { protocol: "http", hostname: "localhost" },
    );
  }

  const cmsUrl = process.env.NEXT_PUBLIC_CMS_API_URL;
  if (cmsUrl) {
    try {
      const { hostname, protocol } = new URL(cmsUrl);
      patterns.push({ protocol: protocol.replace(":", "") as "http" | "https", hostname });
    } catch {
      // invalid URL — skip
    }
  }

  const extraDomains = process.env.NEXT_IMAGE_DOMAINS;
  if (extraDomains) {
    for (const raw of extraDomains.split(",")) {
      const hostname = raw.trim();
      if (hostname) {
        patterns.push({ protocol: "https", hostname });
      }
    }
  }

  return { remotePatterns: patterns };
};

const nextConfig: NextConfig = {
  outputFileTracingRoot: path.resolve(__dirname),
  ...(outputMode && { output: outputMode }),
  logging: {
    fetches: {
      fullUrl: true,
    },
  },
  images: buildImageRemotePatterns(),
};

export default withNextIntl(nextConfig);
