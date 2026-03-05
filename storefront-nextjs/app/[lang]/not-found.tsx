import { getTranslations } from "next-intl/server";

export default async function NotFound() {
  const translate = await getTranslations("NotFound");

  return (
    <div className="py-24 text-center">
      <h1 className="text-2xl font-semibold">{translate("title")}</h1>
      <p className="mt-2 text-sm text-slate-500">{translate("description")}</p>
    </div>
  );
}
