import { DatePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import {
    CommerceNotificationTemplate,
    CommerceNotificationTemplateRequest,
} from '../models/commerce.types';
import { CommerceAdminNotificationTemplateService } from '../services/commerce-admin.service';

type ActiveFilter = '' | 'true' | 'false';

@Component({
    selector: 'spa-commerce-notification-template-list',
    standalone: true,
    imports: [
        DatePipe,
        TranslocoModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        AdminPageHeaderComponent,
        SpaStatusBadgeComponent,
    ],
    templateUrl: './commerce-notification-template-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceNotificationTemplateListComponent implements OnInit {
    readonly #service = inject(CommerceAdminNotificationTemplateService);
    readonly #notification = inject(NotificationService);
    readonly #fb = inject(FormBuilder);

    protected readonly templatesSig = signal<CommerceNotificationTemplate[]>([]);
    protected readonly selectedSig = signal<CommerceNotificationTemplate | null>(null);
    protected readonly previewSig = signal<{ subject: string; content: string } | null>(null);
    protected readonly isLoadingSig = signal(false);
    protected readonly isSavingSig = signal(false);
    protected readonly eventTypeOptions = [
        'ORDER_PAID',
        'ORDER_SHIPPED',
        'ORDER_REQUEST_CREATED',
        'ORDER_REQUEST_APPROVED',
        'ORDER_REQUEST_REJECTED',
    ];

    protected readonly filterForm = this.#fb.nonNullable.group({
        eventType: [''],
        language: [''],
        active: ['' as ActiveFilter],
    });

    protected readonly templateForm = this.#fb.nonNullable.group({
        subject: ['', [Validators.required, Validators.maxLength(255)]],
        content: ['', [Validators.required, Validators.maxLength(20000)]],
        active: [true],
    });

    ngOnInit(): void {
        this.loadTemplates();
    }

    protected loadTemplates(): void {
        this.isLoadingSig.set(true);
        const filters = this.filterForm.getRawValue();
        this.#service
            .list({
                eventType: filters.eventType,
                language: filters.language,
                active: this.activeFilterValue(filters.active),
            })
            .pipe(take(1))
            .subscribe({
                next: (templates) => {
                    this.templatesSig.set(templates);
                    this.isLoadingSig.set(false);
                },
                error: (error) => {
                    this.isLoadingSig.set(false);
                    this.#notification.alert(
                        error?.error?.message || 'admin.commerce.messages.loadFailed'
                    );
                },
            });
    }

    protected selectTemplate(template: CommerceNotificationTemplate): void {
        this.selectedSig.set(template);
        this.previewSig.set(null);
        this.templateForm.reset({
            subject: template.subject,
            content: template.content,
            active: template.active,
        });
    }

    protected saveTemplate(): void {
        const selected = this.selectedSig();
        if (!selected || this.templateForm.invalid || this.isSavingSig()) {
            this.templateForm.markAllAsTouched();
            return;
        }
        const request: CommerceNotificationTemplateRequest = this.templateForm.getRawValue();
        this.isSavingSig.set(true);
        this.#service
            .update(selected.templateUid, request)
            .pipe(take(1))
            .subscribe({
                next: (template) => {
                    this.isSavingSig.set(false);
                    this.selectTemplate(template);
                    this.loadTemplates();
                    this.#notification.success('admin.commerce.messages.notificationTemplateSaved');
                },
                error: (error) => {
                    this.isSavingSig.set(false);
                    this.#notification.alert(
                        error?.error?.message || 'admin.commerce.messages.notificationTemplateSaveFailed'
                    );
                },
            });
    }

    protected preview(template: CommerceNotificationTemplate): void {
        this.#service
            .preview(template.templateUid)
            .pipe(take(1))
            .subscribe({
                next: (preview) => this.previewSig.set({
                    subject: preview.subject,
                    content: preview.content,
                }),
                error: (error) =>
                    this.#notification.alert(
                        error?.error?.message || 'admin.commerce.messages.notificationTemplatePreviewFailed'
                    ),
            });
    }

    protected eventLabelKey(eventType: string): string {
        return `admin.commerce.notificationTemplates.events.${eventType}`;
    }

    protected statusLabel(template: CommerceNotificationTemplate): string {
        return template.active ? 'ACTIVE' : 'INACTIVE';
    }

    private activeFilterValue(value: ActiveFilter): boolean | null {
        if (value === 'true') {
            return true;
        }
        if (value === 'false') {
            return false;
        }
        return null;
    }
}
