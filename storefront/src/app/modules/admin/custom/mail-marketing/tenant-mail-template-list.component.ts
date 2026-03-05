import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import {
    GridAction,
    GridActionEvent,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { NotificationService } from '@shared/notifications/notification.service';
import { MailTemplateTypeSummaryVm } from './mail-marketing.types';
import { TenantMailMarketingService } from './tenant-mail-marketing.service';

@Component({
    selector: 'spa-tenant-mail-template-list',
    standalone: true,
    templateUrl: './tenant-mail-template-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        MatIconModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
    ],
})
export class SpaTenantMailTemplateListComponent implements OnInit {
    readonly #tenantMailMarketingService = inject(TenantMailMarketingService);
    readonly #notificationService = inject(NotificationService);
    readonly #router = inject(Router);
    readonly #activatedRoute = inject(ActivatedRoute);

    protected readonly loadingSig = signal<boolean>(true);
    protected readonly itemsSig = signal<MailTemplateTypeSummaryVm[]>([]);

    protected readonly columns: GridColumn<MailTemplateTypeSummaryVm>[] = [
        {
            key: 'templateType',
            label: 'admin.mailMarketing.fields.templateType',
            type: 'text',
            width: '1fr',
        },
        {
            key: 'languages',
            label: 'admin.mailMarketing.fields.languages',
            type: 'badge',
            getValue: (item) => item.languages.join(', '),
            width: '170px',
        },
        {
            key: 'subscriberCount',
            label: 'admin.mailMarketing.fields.subscriberCount',
            type: 'text',
            width: '130px',
        },
        {
            key: 'lastCampaignAt',
            label: 'admin.mailMarketing.fields.lastCampaignAt',
            type: 'date',
            width: '180px',
        },
    ];

    protected readonly actions: GridAction<MailTemplateTypeSummaryVm>[] = [
        {
            icon: 'heroicons_outline:arrow-top-right-on-square',
            label: 'admin.mailMarketing.actions.openDetail',
            action: 'detail',
        },
    ];

    ngOnInit(): void {
        this.#loadTemplateTypes();
    }

    protected onGridAction(
        event: GridActionEvent<MailTemplateTypeSummaryVm>
    ): void {
        if (event.action !== 'detail') {
            return;
        }
        this.#openDetail(event.item);
    }

    protected onRowClick(item: MailTemplateTypeSummaryVm): void {
        this.#openDetail(item);
    }

    #openDetail(item: MailTemplateTypeSummaryVm): void {
        this.#router.navigate([item.templateType], {
            relativeTo: this.#activatedRoute,
        });
    }

    #loadTemplateTypes(): void {
        this.loadingSig.set(true);
        this.#tenantMailMarketingService.getTemplateTypes().subscribe({
            next: (items) => {
                this.itemsSig.set(items);
                this.loadingSig.set(false);
            },
            error: (error) => {
                this.loadingSig.set(false);
                this.#notificationService.alert(error.error.message);
            },
        });
    }
}
