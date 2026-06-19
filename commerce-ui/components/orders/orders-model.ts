import type { InlineCustomerAuthModel } from "@/components/customer/inline-customer-auth-model";
import { createInlineCustomerAuthModel } from "@/components/customer/inline-customer-auth-model";

export type OrdersModel = {
  eyebrow: string;
  title: string;
  description: string;
  detailTitle: string;
  listTitle: string;
  emptyTitle: string;
  emptyDescription: string;
  loadingLabel: string;
  errorFallback: string;
  rowOrderUid: string;
  rowItems: string;
  rowStatus: string;
  rowCreatedAt: string;
  rowShipping: string;
  rowDelivery: string;
  rowBilling: string;
  rowLegal: string;
  rowTotal: string;
  detailsAction: string;
  primaryAction: string;
  secondaryAction: string;
  itemFallback: string;
  requestTitle: string;
  requestReasonLabel: string;
  requestDescriptionLabel: string;
  requestCancellationAction: string;
  requestReturnAction: string;
  requestSubmitAction: string;
  requestStatusTitle: string;
  requestSuccessLabel: string;
  auth: InlineCustomerAuthModel;
};

type ModelTranslator<TModel> = (key: keyof TModel & string) => string;
type OrdersTranslationModel = OrdersModel & {
  authTitle: string;
  authDescription: string;
};

export const createOrdersModel = (
  translate: ModelTranslator<OrdersTranslationModel>,
  account: ModelTranslator<InlineCustomerAuthModel>,
): OrdersModel => ({
  eyebrow: translate("eyebrow"),
  title: translate("title"),
  description: translate("description"),
  detailTitle: translate("detailTitle"),
  listTitle: translate("listTitle"),
  emptyTitle: translate("emptyTitle"),
  emptyDescription: translate("emptyDescription"),
  loadingLabel: translate("loadingLabel"),
  errorFallback: translate("errorFallback"),
  rowOrderUid: translate("rowOrderUid"),
  rowItems: translate("rowItems"),
  rowStatus: translate("rowStatus"),
  rowCreatedAt: translate("rowCreatedAt"),
  rowShipping: translate("rowShipping"),
  rowDelivery: translate("rowDelivery"),
  rowBilling: translate("rowBilling"),
  rowLegal: translate("rowLegal"),
  rowTotal: translate("rowTotal"),
  detailsAction: translate("detailsAction"),
  primaryAction: translate("primaryAction"),
  secondaryAction: translate("secondaryAction"),
  itemFallback: translate("itemFallback"),
  requestTitle: translate("requestTitle"),
  requestReasonLabel: translate("requestReasonLabel"),
  requestDescriptionLabel: translate("requestDescriptionLabel"),
  requestCancellationAction: translate("requestCancellationAction"),
  requestReturnAction: translate("requestReturnAction"),
  requestSubmitAction: translate("requestSubmitAction"),
  requestStatusTitle: translate("requestStatusTitle"),
  requestSuccessLabel: translate("requestSuccessLabel"),
  auth: createInlineCustomerAuthModel(translate, account),
});
