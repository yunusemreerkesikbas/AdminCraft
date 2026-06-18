export type CheckoutReturnModel = {
  eyebrow: string;
  title: string;
  successTitle: string;
  failureTitle: string;
  unknownTitle: string;
  successDescription: string;
  failureDescription: string;
  unknownDescription: string;
  loadingLabel: string;
  summaryTitle: string;
  summaryNote: string;
  rowStatus: string;
  rowAttempt: string;
  rowOrder: string;
  rowProvider: string;
  rowTotal: string;
  missingValue: string;
  primaryAction: string;
  orderAction: string;
  retryAction: string;
  secondaryAction: string;
  errorFallback: string;
};

type Translator<TModel> = (key: keyof TModel & string) => string;

export const createCheckoutReturnModel = (
  translate: Translator<CheckoutReturnModel>,
): CheckoutReturnModel => ({
  eyebrow: translate("eyebrow"),
  title: translate("title"),
  successTitle: translate("successTitle"),
  failureTitle: translate("failureTitle"),
  unknownTitle: translate("unknownTitle"),
  successDescription: translate("successDescription"),
  failureDescription: translate("failureDescription"),
  unknownDescription: translate("unknownDescription"),
  loadingLabel: translate("loadingLabel"),
  summaryTitle: translate("summaryTitle"),
  summaryNote: translate("summaryNote"),
  rowStatus: translate("rowStatus"),
  rowAttempt: translate("rowAttempt"),
  rowOrder: translate("rowOrder"),
  rowProvider: translate("rowProvider"),
  rowTotal: translate("rowTotal"),
  missingValue: translate("missingValue"),
  primaryAction: translate("primaryAction"),
  orderAction: translate("orderAction"),
  retryAction: translate("retryAction"),
  secondaryAction: translate("secondaryAction"),
  errorFallback: translate("errorFallback"),
});
