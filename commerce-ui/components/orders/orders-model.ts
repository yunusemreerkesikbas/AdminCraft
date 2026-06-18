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
  attentionLabel: string;
  detailsAction: string;
  primaryAction: string;
  secondaryAction: string;
  itemFallback: string;
  auth: InlineCustomerAuthModel;
};

type Translator = (key: string) => string;

export const createOrdersModel = (
  translate: Translator,
  account: Translator,
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
  attentionLabel: translate("attentionLabel"),
  detailsAction: translate("detailsAction"),
  primaryAction: translate("primaryAction"),
  secondaryAction: translate("secondaryAction"),
  itemFallback: translate("itemFallback"),
  auth: createInlineCustomerAuthModel(translate, account),
});
