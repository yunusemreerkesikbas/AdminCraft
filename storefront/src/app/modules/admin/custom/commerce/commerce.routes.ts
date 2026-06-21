import { Routes } from '@angular/router';
import { SpaCommerceDashboardComponent } from './dashboard/commerce-dashboard.component';
import { SpaCommerceOrderDetailComponent } from './orders/commerce-order-detail.component';
import { SpaCommerceOrderListComponent } from './orders/commerce-order-list.component';
import { SpaCommercePaymentAttemptListComponent } from './payment-attempts/commerce-payment-attempt-list.component';
import { SpaCommerceOrderRequestListComponent } from './order-requests/commerce-order-request-list.component';
import { SpaCommerceOrderRequestDetailComponent } from './order-requests/commerce-order-request-detail.component';
import { SpaCommerceLegalTemplateListComponent } from './legal-templates/commerce-legal-template-list.component';

export default [
    {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
    },
    {
        path: 'dashboard',
        component: SpaCommerceDashboardComponent,
    },
    {
        path: 'orders',
        component: SpaCommerceOrderListComponent,
    },
    {
        path: 'orders/:orderUid',
        component: SpaCommerceOrderDetailComponent,
    },
    {
        path: 'payment-attempts',
        component: SpaCommercePaymentAttemptListComponent,
    },
    {
        path: 'order-requests',
        component: SpaCommerceOrderRequestListComponent,
    },
    {
        path: 'order-requests/:requestUid',
        component: SpaCommerceOrderRequestDetailComponent,
    },
    {
        path: 'legal-templates',
        component: SpaCommerceLegalTemplateListComponent,
    },
] as Routes;
