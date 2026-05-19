import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    Injectable,
    TemplateRef,
    ViewChild,
    inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { CrudStore } from '@core/crud/crud-store';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { PlatformOutreachCampaignCreateDialogComponent } from './platform-outreach-campaign-create-dialog.component';
import { PlatformOutreachCampaignOutboxDialogComponent } from './platform-outreach-campaign-outbox-dialog.component';
import { PlatformOutreachService } from './platform-outreach.service';
import {
    CreateOutreachCampaignPayload,
    OutreachCampaignStatus,
    OutreachCampaignVm,
} from './platform-outreach.types';

@Injectable({ providedIn: 'root' })
class OutreachCampaignCrudService extends CrudHttpService<
    OutreachCampaignVm,
    CreateOutreachCampaignPayload,
    CreateOutreachCampaignPayload
> {
    protected override endpoints: CrudEndpoints = {
        list: 'platformOutreachCampaigns',
        getById: 'platformOutreachCampaignById',
        create: 'platformOutreachCampaigns',
        update: 'platformOutreachCampaignById',
        delete: 'platformOutreachCampaignById',
    };
}

@Component({
    selector: 'spa-platform-outreach-campaign-list',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './platform-outreach-campaign-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaPlatformOutreachCampaignListComponent extends BasePaginatedListComponent<
    OutreachCampaignVm,
    CreateOutreachCampaignPayload,
    CreateOutreachCampaignPayload
> {
    @ViewChild('statusTemplate', { static: true })
    statusTemplate!: TemplateRef<any>;
    @ViewChild('countsTemplate', { static: true })
    countsTemplate!: TemplateRef<any>;

    readonly #outreachService = inject(PlatformOutreachService);
    readonly #dialog = inject(MatDialog);
    readonly #notify = inject(NotificationService);
    readonly #transloco = inject(TranslocoService);

    protected override service = inject(OutreachCampaignCrudService);
    protected override store = new CrudStore<OutreachCampaignVm>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected columns: GridColumn<OutreachCampaignVm>[] = [];
    protected actions: GridAction<OutreachCampaignVm>[] = [];

    protected override onInit(): void {
        this.columns = [
            {
                key: 'name',
                label: 'admin.outreach.campaigns.fields.name',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'subject',
                label: 'admin.outreach.campaigns.fields.subject',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'status',
                label: 'admin.outreach.campaigns.fields.status',
                type: 'custom',
                template: this.statusTemplate,
                width: '180px',
            },
            {
                key: 'totalCount',
                label: 'admin.outreach.campaigns.fields.counts',
                type: 'custom',
                template: this.countsTemplate,
                width: '160px',
            },
            {
                key: 'createdByEmail',
                label: 'admin.outreach.campaigns.fields.createdBy',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'createdAt',
                label: 'admin.outreach.campaigns.fields.createdAt',
                type: 'date',
                width: '180px',
            },
        ];

        this.actions = [
            {
                icon: 'heroicons_outline:paper-airplane',
                label: 'admin.outreach.campaigns.actions.send',
                action: 'send',
            },
            {
                icon: 'heroicons_outline:inbox',
                label: 'admin.outreach.campaigns.actions.viewLog',
                action: 'viewLog',
            },
        ];
    }

    protected override onLoadError(): void {}

    protected onGridAction(event: {
        action: string;
        item: OutreachCampaignVm;
    }): void {
        if (event.action === 'send') {
            this.#sendCampaign(event.item);
            return;
        }
        if (event.action === 'viewLog') {
            this.#openOutboxDialog(event.item);
        }
    }

    protected createCampaign(): void {
        const dialogRef = this.#dialog.open(
            PlatformOutreachCampaignCreateDialogComponent,
            {
                width: '800px',
                maxWidth: 'calc(100vw - 2rem)',
                maxHeight: '90vh',
                panelClass: 'spa-compact-dialog',
                disableClose: true,
            }
        );

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadItems();
                }
            });
    }

    protected campaignStatusClass(status: OutreachCampaignStatus): string {
        switch (status) {
            case 'COMPLETED':
                return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200';
            case 'COMPLETED_WITH_ERRORS':
                return 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-200';
            case 'FAILED':
                return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200';
            case 'SENDING':
                return 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200';
            default:
                return 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400';
        }
    }

    #sendCampaign(campaign: OutreachCampaignVm): void {
        if (campaign.status !== 'DRAFT') {
            return;
        }

        if (
            !confirm(
                this.#transloco.translate(
                    'admin.outreach.campaigns.messages.confirmSend',
                    {
                        name: campaign.name,
                    }
                )
            )
        ) {
            return;
        }

        this.#outreachService
            .sendCampaign(campaign.id)
            .pipe(take(1))
            .subscribe({
                next: () => this.loadItems(),
                error: (error: any) =>
                    this.#notify.alert(error?.error?.message ?? ''),
            });
    }

    #openOutboxDialog(campaign: OutreachCampaignVm): void {
        this.#dialog.open(PlatformOutreachCampaignOutboxDialogComponent, {
            width: '700px',
            maxWidth: 'calc(100vw - 2rem)',
            data: {
                campaignId: campaign.id,
                campaignName: campaign.name,
                campaign,
            },
        });
    }
}
