import type { MetadataRoute } from "next";
import { resolveCmsEndpoint } from "@/lib/core/http/endpoints";
import { fetchCmsText } from "@/lib/core/http/fetch-text";

type RobotsRule = {
  userAgent: string | string[];
  allow?: string | string[];
  disallow?: string | string[];
};

type MutableRobotsRule = {
  userAgents: string[];
  allow?: string | string[];
  disallow?: string | string[];
};

const SAFE_ROBOTS: MetadataRoute.Robots = {
  rules: {
    userAgent: "*",
    disallow: "/",
  },
};

const appendDirective = (
  current: string | string[] | undefined,
  value: string,
): string | string[] => {
  if (!current) {
    return value;
  }

  return Array.isArray(current) ? [...current, value] : [current, value];
};

const parseRobots = (text: string): MetadataRoute.Robots => {
  const rules: RobotsRule[] = [];
  const sitemap: string[] = [];
  let host: string | undefined;
  let activeRule: MutableRobotsRule | null = null;

  const finalizeRule = () => {
    if (!activeRule || activeRule.userAgents.length === 0) {
      activeRule = null;
      return;
    }

    rules.push({
      userAgent:
        activeRule.userAgents.length === 1 ? activeRule.userAgents[0] : activeRule.userAgents,
      allow: activeRule.allow,
      disallow: activeRule.disallow,
    });
    activeRule = null;
  };

  for (const rawLine of text.split(/\r?\n/)) {
    const line = rawLine.replace(/#.*$/, "").trim();
    if (!line) {
      finalizeRule();
      continue;
    }

    const separatorIndex = line.indexOf(":");
    if (separatorIndex < 0) {
      continue;
    }

    const key = line.slice(0, separatorIndex).trim().toLowerCase();
    const value = line.slice(separatorIndex + 1).trim();

    switch (key) {
      case "user-agent":
        if (activeRule && (activeRule.allow !== undefined || activeRule.disallow !== undefined)) {
          finalizeRule();
        }
        if (!activeRule) {
          activeRule = { userAgents: [] };
        }
        activeRule.userAgents.push(value || "*");
        break;
      case "allow":
        if (!activeRule) {
          activeRule = { userAgents: ["*"] };
        }
        activeRule.allow = appendDirective(activeRule.allow, value);
        break;
      case "disallow":
        if (!activeRule) {
          activeRule = { userAgents: ["*"] };
        }
        activeRule.disallow = appendDirective(activeRule.disallow, value);
        break;
      case "sitemap":
        if (value) {
          sitemap.push(value);
        }
        break;
      case "host":
        if (value) {
          host = value;
        }
        break;
      default:
        break;
    }
  }

  finalizeRule();

  if (rules.length === 0) {
    return SAFE_ROBOTS;
  }

  return {
    rules,
    sitemap: sitemap.length === 0 ? undefined : sitemap.length === 1 ? sitemap[0] : sitemap,
    host,
  };
};

export default async function robots(): Promise<MetadataRoute.Robots> {
  try {
    const content = await fetchCmsText(resolveCmsEndpoint("cmsRobotsTxt"), undefined, {
      revalidate: 3600,
    });
    return content ? parseRobots(content) : SAFE_ROBOTS;
  } catch (error) {
    console.warn("[robots] Falling back to safe robots rules because CMS is unavailable.", error);
    return SAFE_ROBOTS;
  }
}
