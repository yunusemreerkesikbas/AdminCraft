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
import { NotificationService } from '@shared/notifications/notification.service';
import { MailTemplateTypeSummaryVm } from './mail-marketing.types';
import { PlatformMailMarketingService } from './platform-mail-marketing.service';

@Component({
    selector: 'spa-platform-mail-template-list',
    standalone: true,
    templateUrl: './platform-mail-template-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        MatIconModule,
        TranslocoModule,
        AdminPageHeaderComponent,
    ],
})
export class SpaPlatformMailTemplateListComponent implements OnInit {
    readonly #platformMailMarketingService = inject(
        PlatformMailMarketingService
    );
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
        this.#platformMailMarketingService.getTemplateTypes().subscribe({
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
