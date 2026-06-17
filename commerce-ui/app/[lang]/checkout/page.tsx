import { getTranslations } from "next-intl/server";
import { PageShell } from "@/components/ui/PageShell";
import {
  ActionLink,
  DisabledAction,
  StepFrame,
} from "@/components/ui/StorefrontPrimitives";
import { withLocalePath } from "@/lib/core/i18n/locale";

export default async function CheckoutPage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const translate = await getTranslations("Checkout");

  return (
    <PageShell
      eyebrow={translate("eyebrow")}
      title={translate("title")}
      description={translate("description")}
      actions={
        <>
          <DisabledAction label={translate("primaryDisabled")} />
          <ActionLink
            href={withLocalePath(lang, "cart")}
            label={translate("secondaryAction")}
            variant="secondary"
          />
        </>
      }
      visual={
        <StepFrame
          title={translate("stepsTitle")}
          steps={[
            {
              title: translate("stepAddressTitle"),
              description: translate("stepAddressDescription"),
            },
            {
              title: translate("stepPaymentTitle"),
              description: translate("stepPaymentDescription"),
            },
            {
              title: translate("stepReviewTitle"),
              description: translate("stepReviewDescription"),
            },
          ]}
        />
      }
    />
  );
}
