import type { NextRequest } from "next/server";
import { proxyTenantMediaRequest } from "@/lib/core/media/proxy";

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> },
): Promise<Response> {
  return proxyTenantMediaRequest(request, params);
}
