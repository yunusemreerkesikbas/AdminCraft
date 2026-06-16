import { Routes } from '@angular/router';
import { SpaCommerceDashboardComponent } from './dashboard/commerce-dashboard.component';
import { SpaCommerceOrderDetailComponent } from './orders/commerce-order-detail.component';
import { SpaCommerceOrderListComponent } from './orders/commerce-order-list.component';
import { SpaCommercePaymentAttemptListComponent } from './payment-attempts/commerce-payment-attempt-list.component';

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
] as Routes;
