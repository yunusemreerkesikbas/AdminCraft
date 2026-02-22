import { DatePipe, DecimalPipe, NgClass } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { UrlValidator } from '@shared/utils/url-validator';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { take } from 'rxjs';
import { SiteService } from '../../site.service';
import { SiteOverviewResponse } from '../../site.types';

@Component({
    selector: 'spa-site-overview',
    templateUrl: './site-overview.component.html',
    styleUrls: ['./site-overview.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        NgClass,
        DatePipe,
        DecimalPipe,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
    ],
})
export class SpaSiteOverviewComponent {
    readonly #confirmation = inject(ConfirmationService);
    readonly #notification = inject(NotificationService);
    readonly #siteService = inject(SiteService);
    readonly #tenantService = inject(TenantContextService);

    tenant = this.#tenantService.tenant;

    @Input() overview: SiteOverviewResponse | null = null;
    @Output() refresh = new EventEmitter<void>();

    statusClass =
        'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300';
    statusIcon = 'heroicons_outline:document';

    getActivityIcon(entityType: string): string {
        switch (entityType) {
            case 'PAGE':
                return 'heroicons_outline:document-text';
            case 'COMPONENT':
                return 'heroicons_outline:cube';
            case 'MEDIA':
                return 'heroicons_outline:photo';
            case 'PRODUCT':
                return 'heroicons_outline:shopping-bag';
            case 'SITE':
            case 'SITE_SETTINGS':
                return 'heroicons_outline:cog-6-tooth';
            case 'NAVIGATION':
                return 'heroicons_outline:bars-3';
            default:
                return 'heroicons_outline:document';
        }
    }

    getActivityClass(action: string): string {
        switch (action) {
            case 'CREATED':
                return 'text-green-600';
            case 'UPDATED':
                return 'text-blue-600';
            case 'DELETED':
                return 'text-red-600';
            case 'PUBLISHED':
                return 'text-purple-600';
            case 'UNPUBLISHED':
                return 'text-orange-600';
            default:
                return 'text-gray-600';
        }
    }

    onRefresh(): void {
        this.refresh.emit();
    }

    onPreview(): void {
        const previewUrl = this.overview?.actions?.previewUrl;
        if (!previewUrl) return;

        if (UrlValidator.isValidPreviewUrl(previewUrl)) {
            window.open(previewUrl, '_blank', 'noopener,noreferrer');
        } else {
            this.#notification.alert(
                'admin.site.dashboard.overview.actions.invalidPreviewUrl'
            );
        }
    }

    onEnableMaintenance(): void {
        if (!this.overview?.actions?.canEnableMaintenance || !this.overview?.id)
            return;

        this.#confirmation
            .confirm(
                'admin.site.dashboard.overview.confirmMaintenance.title',
                'admin.site.dashboard.overview.confirmMaintenance.message',
                'admin.site.dashboard.overview.actions.maintenance',
                'warning'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (confirmed && this.overview?.id) {
                    this.#siteService
                        .enableMaintenanceMode(this.overview.id)
                        .pipe(take(1))
                        .subscribe({
                            next: (response) => {
                                this.#notification.success(response.message);
                                this.refresh.emit();
                            },
                            error: (error) => {
                                this.#notification.alert(error?.error?.message);
                            },
                        });
                }
            });
    }

    onDisableMaintenance(): void {
        if (!this.overview?.id) return;

        this.#confirmation
            .confirm(
                'admin.site.dashboard.overview.confirmDisableMaintenance.title',
                'admin.site.dashboard.overview.confirmDisableMaintenance.message',
                'admin.site.dashboard.overview.actions.disableMaintenance',
                'info'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (confirmed && this.overview?.id) {
                    this.#siteService
                        .disableMaintenanceMode(this.overview.id)
                        .pipe(take(1))
                        .subscribe({
                            next: (response) => {
                                this.#notification.success(response.message);
                                this.refresh.emit();
                            },
                            error: (error) => {
                                this.#notification.alert(error?.error?.message);
                            },
                        });
                }
            });
    }
}
