import { redirect } from "next/navigation";
import { FALLBACK_LOCALE } from "@/lib/core/i18n/locale";

export default function RootPage() {
  redirect(`/${FALLBACK_LOCALE}`);
}
