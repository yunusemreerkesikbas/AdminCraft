import type { NextConfig } from "next";
import path from "path";
import createNextIntlPlugin from "next-intl/plugin";

const withNextIntl = createNextIntlPlugin("./i18n/request.ts");

const outputMode = process.env.NEXT_OUTPUT as "export" | "standalone" | undefined;

const nextConfig: NextConfig = {
  outputFileTracingRoot: path.resolve(__dirname),
  ...(outputMode && { output: outputMode }),
  logging: {
    fetches: {
      fullUrl: true,
    },
  },
  images: {
    remotePatterns: [
      { protocol: "http",  hostname: "127.0.0.1" },
      { protocol: "http",  hostname: "localhost" },
      { protocol: "https", hostname: "*.craftive.io" },
    ],
  },
};

export default withNextIntl(nextConfig);
