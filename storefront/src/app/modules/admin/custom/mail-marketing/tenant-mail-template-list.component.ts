import { DatePipe, NgClass } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { MailTemplateTypeSummaryVm } from './mail-marketing.types';
import { TenantMailMarketingService } from './tenant-mail-marketing.service';

@Component({
    selector: 'spa-tenant-mail-template-list',
    standalone: true,
    templateUrl: './tenant-mail-template-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        DatePipe,
        NgClass,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        AdminPageHeaderComponent,
    ],
})
export class SpaTenantMailTemplateListComponent implements OnInit {
    readonly #tenantMailMarketingService = inject(TenantMailMarketingService);
    readonly #notificationService = inject(NotificationService);
    readonly #router = inject(Router);
    readonly #activatedRoute = inject(ActivatedRoute);

    protected readonly loadingSig = signal<boolean>(true);
    protected readonly itemsSig = signal<MailTemplateTypeSummaryVm[]>([]);

    ngOnInit(): void {
        this.#loadTemplateTypes();
    }

    protected onCardClick(item: MailTemplateTypeSummaryVm): void {
        this.#router.navigate([item.templateType], {
            relativeTo: this.#activatedRoute,
        });
    }

    protected onRefresh(): void {
        this.#loadTemplateTypes();
    }

    protected humanName(templateType: string): string {
        const map: Record<string, string> = {
            NEWSLETTER_DEFAULT: 'Newsletter',
            VERSION_UPGRADE: 'Version Upgrade',
            TENANT_USER_WELCOME: 'User Welcome',
        };
        return map[templateType] ?? templateType;
    }

    protected templateIcon(templateType: string): string {
        const map: Record<string, string> = {
            NEWSLETTER_DEFAULT: 'mail',
            VERSION_UPGRADE: 'system_update',
            TENANT_USER_WELCOME: 'person_add',
        };
        return map[templateType] ?? 'mail';
    }

    #loadTemplateTypes(): void {
        this.loadingSig.set(true);
        this.#tenantMailMarketingService.getTemplateTypes().pipe(take(1)).subscribe({
            next: (items) => {
                this.itemsSig.set(items);
                this.loadingSig.set(false);
            },
            error: (error: any) => {
                this.loadingSig.set(false);
                this.#notificationService.alert(error?.error?.message ?? '');
            },
        });
    }
}
